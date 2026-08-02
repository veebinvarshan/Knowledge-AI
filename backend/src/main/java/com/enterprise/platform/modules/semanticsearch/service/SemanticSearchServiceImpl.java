package com.enterprise.platform.modules.semanticsearch.service;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.core.config.properties.SemanticSearchProperties;
import com.enterprise.platform.modules.search.provider.SearchResult;
import com.enterprise.platform.modules.search.service.HybridRankingService;
import com.enterprise.platform.modules.search.service.SearchAuthorizationGuard;
import com.enterprise.platform.modules.search.service.SearchService;
import com.enterprise.platform.modules.semanticsearch.domain.*;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchEvents.*;
import com.enterprise.platform.modules.semanticsearch.provider.SemanticProviderResolver;
import com.enterprise.platform.modules.semanticsearch.provider.SemanticSearchProvider;
import com.enterprise.platform.modules.semanticsearch.repository.SemanticSearchAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.*;

@Service
@Transactional
public class SemanticSearchServiceImpl implements SemanticSearchService {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchServiceImpl.class);

    private final SemanticSearchAnalyticsRepository analyticsRepository;
    private final SearchAuthorizationGuard authorizationGuard;
    private final QueryEmbeddingService queryEmbeddingService;
    private final QueryCacheService cacheService;
    private final SemanticProviderResolver providerResolver;
    private final SearchService searchService;
    private final HybridRankingService hybridRankingService;
    private final ApplicationEventPublisher eventPublisher;

    private final SemanticSearchProperties properties;
    private final EmbeddingProperties embeddingProperties;

    public SemanticSearchServiceImpl(
            SemanticSearchAnalyticsRepository analyticsRepository,
            SearchAuthorizationGuard authorizationGuard,
            QueryEmbeddingService queryEmbeddingService,
            QueryCacheService cacheService,
            SemanticProviderResolver providerResolver,
            SearchService searchService,
            HybridRankingService hybridRankingService,
            ApplicationEventPublisher eventPublisher,
            SemanticSearchProperties properties,
            EmbeddingProperties embeddingProperties) {
        this.analyticsRepository = analyticsRepository;
        this.authorizationGuard = authorizationGuard;
        this.queryEmbeddingService = queryEmbeddingService;
        this.cacheService = cacheService;
        this.providerResolver = providerResolver;
        this.searchService = searchService;
        this.hybridRankingService = hybridRankingService;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.embeddingProperties = embeddingProperties;
    }

    @Override
    public SemanticSearchResult search(String tenantId, String permissionHash, SemanticSearchRequest request) {
        long start = System.currentTimeMillis();

        if (!properties.enabled()) {
            throw new IllegalStateException("Semantic search pipeline is disabled.");
        }

        // 1. Security & permission checks (Refinement 11)
        if (!authorizationGuard.authorizeSearch(tenantId, permissionHash)) {
            throw new SecurityException("Unauthorized search request.");
        }

        // 2. Query Normalization (Refinement 7)
        String normalizedQuery = normalizeQuery(request.query());

        // 3. Normalized Query Hash (Refinement 10)
        String queryHash = hashQuery(normalizedQuery);

        String cacheKey = "query_cache:" + tenantId + ":" + queryHash + ":" + request.limit();

        // 4. Cache Lookup (Refinement 2 & 13)
        SemanticSearchResult cached = (SemanticSearchResult) cacheService.get(cacheKey);
        if (cached != null) {
            log.info("Query cache hit for key: {}", cacheKey);
            recordAnalytics(tenantId, request.userId(), queryHash, cached.matches().size(), System.currentTimeMillis() - start, true);
            return cached;
        }

        SemanticSearchJob job = new SemanticSearchJob(
                tenantId,
                request.userId(),
                queryHash,
                properties.provider(),
                embeddingProperties.modelName(),
                properties.similarityMetric()
        );
        job = analyticsRepository.save(job);

        publishAfterCommit(new SemanticSearchRequestedEvent(
                job.getJobId(), job.getTenantId(), job.getProvider(), job.getEmbeddingModel(), job.getSimilarityMetric()
        ));

        try {
            job.transitionToEmbedding();
            analyticsRepository.saveAndFlush(job);

            // 5. Generate embedding vector
            float[] queryVector = queryEmbeddingService.generateQueryVector(normalizedQuery);

            publishAfterCommit(new QueryEmbeddingGeneratedEvent(
                    job.getJobId(), job.getTenantId(), job.getProvider(), job.getEmbeddingModel(), job.getSimilarityMetric()
            ));

            job.transitionToSearching();
            analyticsRepository.saveAndFlush(job);

            // 6. Vector Retrieval
            SemanticSearchProvider provider = providerResolver.resolve(properties.provider());
            SemanticSearchResult semanticResult = provider.searchSemantic(queryVector, tenantId, request.filters(), request.limit());

            SemanticSearchResult finalResult = semanticResult;

            // 7. Hybrid Integration (Refinement 6)
            if ("HYBRID".equalsIgnoreCase(properties.mode())) {
                // Perform lexical Lucene search
                SearchResult lexicalResult = searchService.search(normalizedQuery, tenantId, permissionHash, request.limit());
                
                // Map lexical Result to SemanticSearchResult format (matches vector structure)
                List<SearchResult.Match> lexicalMatches = lexicalResult.matches();
                List<SearchResult.Match> semanticMatches = new ArrayList<>();
                for (SemanticSearchResult.Match m : semanticResult.matches()) {
                    semanticMatches.add(new SearchResult.Match(
                            m.documentId(), m.versionId(), tenantId, m.title(), m.filename(), m.semanticScore(), m.highlights(), m.metadataSummary()
                    ));
                }
                SearchResult mappedSemanticResult = new SearchResult(semanticMatches, semanticResult.totalHits(), semanticResult.facets());

                // Blending via HybridRankingService (No duplicate RRF logic)
                SearchResult blended = hybridRankingService.performRrf(
                        lexicalResult,
                        mappedSemanticResult,
                        1.0, // bm25Weight
                        1.0, // vectorWeight
                        60,  // k
                        request.limit()
                );

                // Map back to output formats
                List<SemanticSearchResult.Match> blendedMatches = new ArrayList<>();
                for (SearchResult.Match bm : blended.matches()) {
                    // Try to find the semantic match
                    SemanticSearchResult.Match sm = semanticResult.matches().stream()
                            .filter(m -> m.documentId().equals(bm.documentId()))
                            .findFirst().orElse(null);

                    blendedMatches.add(new SemanticSearchResult.Match(
                            bm.documentId(),
                            bm.versionId(),
                            bm.title(),
                            bm.filename(),
                            sm != null ? sm.semanticScore() : 0.0,
                            bm.score(), // hybrid score
                            sm != null ? sm.snippet() : "",
                            bm.highlights(),
                            sm != null ? sm.metadataSummary() : new HashMap<>()
                    ));
                }
                finalResult = new SemanticSearchResult(blendedMatches, blended.totalHits(), blended.facets());
            }

            long executionTime = System.currentTimeMillis() - start;
            job.transitionToCompleted(finalResult.matches().size(), executionTime, false);
            analyticsRepository.save(job);

            // Put results into Redis or local memory fallback
            cacheService.put(cacheKey, finalResult);

            publishAfterCommit(new SemanticSearchCompletedEvent(
                    job.getJobId(), job.getTenantId(), job.getProvider(), job.getEmbeddingModel(),
                    job.getSimilarityMetric(), false, executionTime, finalResult.matches().size()
            ));

            return finalResult;

        } catch (Throwable t) {
            log.error("Semantic search execution failed for job: {}", job.getJobId(), t);
            job.transitionToFailed();
            analyticsRepository.save(job);

            publishAfterCommit(new SemanticSearchFailedEvent(
                    job.getJobId(), job.getTenantId(), job.getProvider(), job.getEmbeddingModel(), job.getSimilarityMetric(), t.getMessage()
            ));
            throw new RuntimeException(t);
        }
    }

    private String normalizeQuery(String query) {
        if (query == null) return "";
        // Unicode Normalization Form D (NFD)
        String decomposed = Normalizer.normalize(query, Normalizer.Form.NFD);
        // strip accents and lowercase/punctuation clean
        return decomposed.replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[\\p{Punct}]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String hashQuery(String query) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(query.getBytes());
            return HexFormat.of().formatHex(encoded);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void recordAnalytics(String tenantId, UUID userId, String queryHash, int resultCount, long time, boolean cacheHit) {
        SemanticSearchJob completedJob = new SemanticSearchJob(
                tenantId, userId, queryHash, properties.provider(), embeddingProperties.modelName(), properties.similarityMetric()
        );
        completedJob.transitionToEmbedding();
        completedJob.transitionToSearching();
        completedJob.transitionToCompleted(resultCount, time, cacheHit);
        analyticsRepository.save(completedJob);

        publishAfterCommit(new SemanticSearchCompletedEvent(
                completedJob.getJobId(), completedJob.getTenantId(), completedJob.getProvider(), completedJob.getEmbeddingModel(),
                completedJob.getSimilarityMetric(), cacheHit, time, resultCount
        ));
    }

    private void publishAfterCommit(Object event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishEvent(event);
                }
            });
        } else {
            eventPublisher.publishEvent(event);
        }
    }
}
