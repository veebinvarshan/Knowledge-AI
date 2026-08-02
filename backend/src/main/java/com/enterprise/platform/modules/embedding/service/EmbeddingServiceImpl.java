package com.enterprise.platform.modules.embedding.service;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.core.config.properties.EmbeddingWorkerProperties;
import com.enterprise.platform.modules.embedding.domain.*;
import com.enterprise.platform.modules.embedding.domain.EmbeddingEvents.*;
import com.enterprise.platform.modules.embedding.provider.EmbeddingProvider;
import com.enterprise.platform.modules.embedding.provider.EmbeddingResult;
import com.enterprise.platform.modules.embedding.repository.EmbeddingChunkRepository;
import com.enterprise.platform.modules.embedding.repository.EmbeddingJobRepository;
import com.enterprise.platform.modules.search.domain.SearchDocument;
import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import com.enterprise.platform.modules.search.repository.SearchDocumentRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Transactional
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingServiceImpl.class);

    private final EmbeddingJobRepository jobRepository;
    private final EmbeddingChunkRepository chunkRepository;
    private final SearchDocumentRepository searchDocumentRepository;
    private final QdrantSearchProvider qdrantSearchProvider;
    private final EmbeddingProviderResolver providerResolver;
    private final ChunkingService chunkingService;
    private final EmbeddingVersionService versionService;
    private final ApplicationEventPublisher eventPublisher;

    private final EmbeddingProperties properties;
    private final ThreadPoolTaskExecutor taskExecutor;

    public EmbeddingServiceImpl(
            EmbeddingJobRepository jobRepository,
            EmbeddingChunkRepository chunkRepository,
            SearchDocumentRepository searchDocumentRepository,
            ObjectProvider<QdrantSearchProvider> qdrantSearchProviderProvider,
            EmbeddingProviderResolver providerResolver,
            ChunkingService chunkingService,
            EmbeddingVersionService versionService,
            ApplicationEventPublisher eventPublisher,
            EmbeddingProperties properties,
            EmbeddingWorkerProperties workerProperties) {
        this.jobRepository = jobRepository;
        this.chunkRepository = chunkRepository;
        this.searchDocumentRepository = searchDocumentRepository;
        this.qdrantSearchProvider = qdrantSearchProviderProvider.getIfAvailable();
        this.providerResolver = providerResolver;
        this.chunkingService = chunkingService;
        this.versionService = versionService;
        this.eventPublisher = eventPublisher;
        this.properties = properties;

        // Bounded worker queue with backpressure CallerRunsPolicy
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(workerProperties.threads());
        this.taskExecutor.setMaxPoolSize(workerProperties.threads());
        this.taskExecutor.setQueueCapacity(workerProperties.queueCapacity());
        this.taskExecutor.setThreadNamePrefix("embedding-worker-");
        this.taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.taskExecutor.initialize();
    }

    @Override
    public EmbeddingJob submitEmbeddingJob(UUID documentId, UUID versionId, String tenantId) {
        if (!properties.enabled()) {
            log.info("Embedding pipeline is disabled; bypassing job submission.");
            return null;
        }

        EmbeddingJob job = new EmbeddingJob(
                documentId,
                versionId,
                tenantId,
                properties.provider(),
                properties.modelName(),
                properties.modelVersion()
        );
        job = jobRepository.save(job);

        publishAfterCommit(new EmbeddingGenerationRequestedEvent(
                job.getJobId(),
                job.getDocumentId(),
                job.getVersionId(),
                job.getTenantId(),
                job.getProvider(),
                job.getModelName(),
                job.getModelVersion()
        ));

        // Enqueue async execution
        UUID jobId = job.getJobId();
        taskExecutor.submit(() -> executeEmbedding(jobId));

        return job;
    }

    @Override
    public void executeEmbedding(UUID jobId) {
        EmbeddingJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        try {
            job.transitionToEmbedding();
            jobRepository.saveAndFlush(job);

            publishAfterCommit(new EmbeddingGenerationStartedEvent(
                    job.getJobId(),
                    job.getDocumentId(),
                    job.getVersionId(),
                    job.getTenantId(),
                    job.getProvider(),
                    job.getModelName(),
                    job.getModelVersion()
            ));

            // Load canonical SearchDocument (Refinement 3)
            SearchDocument searchDoc = searchDocumentRepository.findByVersionId(job.getVersionId()).orElse(null);
            if (searchDoc == null) {
                String error = "No canonical SearchDocument found for version " + job.getVersionId();
                job.transitionToFailed(error);
                jobRepository.save(job);
                publishAfterCommit(new EmbeddingFailedEvent(
                        job.getJobId(), job.getDocumentId(), job.getVersionId(), job.getTenantId(),
                        job.getProvider(), job.getModelName(), job.getModelVersion(), error
                ));
                return;
            }

            String content = searchDoc.getNormalizedText();
            if (content == null || content.isBlank()) {
                job.transitionToSkipped("SearchDocument contains no indexed normalized content.");
                jobRepository.save(job);
                publishAfterCommit(new EmbeddingSkippedEvent(
                        job.getJobId(), job.getDocumentId(), job.getVersionId(), job.getTenantId(),
                        job.getProvider(), job.getModelName(), job.getModelVersion(), "Empty source text"
                ));
                return;
            }

            String sourceChecksum = searchDoc.getFilename() + "_" + content.hashCode(); // Simple sourceChecksum representation

            // Check if version is stale (Refinement 9)
            boolean stale = versionService.areEmbeddingsStale(
                    job.getVersionId(),
                    job.getModelName(),
                    job.getModelVersion(),
                    sourceChecksum
            );

            if (!stale) {
                job.transitionToSkipped("Embeddings are already up-to-date.");
                jobRepository.save(job);
                publishAfterCommit(new EmbeddingSkippedEvent(
                        job.getJobId(), job.getDocumentId(), job.getVersionId(), job.getTenantId(),
                        job.getProvider(), job.getModelName(), job.getModelVersion(), "Embeddings already generated"
                ));
                return;
            }

            // Clean previous chunks/vectors transactionally (Refinement 2)
            chunkRepository.deleteAllByVersionId(job.getVersionId());
            qdrantSearchProvider.deleteVectors(job.getVersionId());

            // Decompose content using configured ChunkingStrategy
            ChunkingStrategy strategy = ChunkingStrategy.valueOf(
                    System.getProperty("platform.embedding.chunking.strategy", "HYBRID").toUpperCase()
            );
            List<ChunkingService.Chunk> parsedChunks = chunkingService.chunkText(content, strategy);

            List<String> textList = new ArrayList<>();
            for (ChunkingService.Chunk c : parsedChunks) {
                textList.add(c.text());
            }

            // Resolve provider and generate embeddings
            EmbeddingProvider provider = providerResolver.resolve(job.getProvider());
            EmbeddingResult result = provider.generate(textList);

            List<float[]> vectors = result.embeddings();
            List<EmbeddingChunk> persistedChunks = new ArrayList<>();

            // Write chunks and populate vectors in Qdrant (Refinement 10)
            for (int i = 0; i < parsedChunks.size(); i++) {
                ChunkingService.Chunk parsed = parsedChunks.get(i);
                float[] vector = vectors.get(i);

                String textHash = HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256").digest(parsed.text().getBytes())
                );

                EmbeddingChunk chunk = EmbeddingChunk.builder()
                        .documentId(job.getDocumentId())
                        .versionId(job.getVersionId())
                        .chunkIndex(i)
                        .tokenCount(parsed.text().length() / 4) // simple token estimate
                        .characterCount(parsed.text().length())
                        .startOffset(parsed.startOffset())
                        .endOffset(parsed.endOffset())
                        .textHash(textHash)
                        .chunkHash(textHash)
                        .sourceVersion(job.getVersionId())
                        .sourceChecksum(sourceChecksum)
                        .sourceLength(content.length())
                        .embeddingModel(result.modelName())
                        .embeddingModelVersion(result.modelVersion())
                        .chunkText(parsed.text())
                        .build();

                chunkRepository.save(chunk);
                persistedChunks.add(chunk);

                // Populate vector to Qdrant (Refinement 10)
                if (qdrantSearchProvider != null) {
                    qdrantSearchProvider.upsertVector(
                            job.getDocumentId(),
                            job.getVersionId(),
                            job.getTenantId(),
                            chunk.getChunkId(),
                            i,
                            vector,
                            result.modelName(),
                            result.modelVersion(),
                            sourceChecksum
                    );
                } else {
                    log.warn("QdrantSearchProvider is not available; skipping vector upsert for chunk {}.", chunk.getChunkId());
                }
            }

            // Transactional replacement of SearchDocument to update its vectorStatus -> GENERATED
            searchDocumentRepository.deleteById(searchDoc.getDocumentId());
            SearchDocument updatedDoc = SearchDocument.builder()
                    .documentId(searchDoc.getDocumentId())
                    .versionId(searchDoc.getVersionId())
                    .tenantId(searchDoc.getTenantId())
                    .title(searchDoc.getTitle())
                    .filename(searchDoc.getFilename())
                    .mimeType(searchDoc.getMimeType())
                    .language(searchDoc.getLanguage())
                    .author(searchDoc.getAuthor())
                    .ocrText(searchDoc.getOcrText())
                    .metadataText(searchDoc.getMetadataText())
                    .extractedKeywords(searchDoc.getExtractedKeywords())
                    .normalizedText(searchDoc.getNormalizedText())
                    .indexedAt(searchDoc.getIndexedAt())
                    .currentVersion(searchDoc.getCurrentVersion())
                    .permissionHash(searchDoc.getPermissionHash())
                    .vectorId(UUID.randomUUID())
                    .vectorStatus("GENERATED")
                    .embeddingModel(result.modelName())
                    .embeddingVersion(result.modelVersion())
                    .generatedAt(Instant.now())
                    .searchMetadata(searchDoc.getSearchMetadata())
                    .build();
            searchDocumentRepository.save(updatedDoc);

            job.transitionToCompleted(persistedChunks.size());
            jobRepository.save(job);

            publishAfterCommit(new EmbeddingCompletedEvent(
                    job.getJobId(), job.getDocumentId(), job.getVersionId(), job.getTenantId(),
                    job.getProvider(), job.getModelName(), job.getModelVersion(), persistedChunks.size()
            ));

        } catch (Throwable t) {
            log.error("Embedding generation failed for job: {}", jobId, t);
            if (isRetryable(t) && job.getRetryCount() < properties.retryCount()) {
                job.transitionToFailed(t.getMessage());
                job.transitionToRetrying();
                jobRepository.save(job);
                publishAfterCommit(new EmbeddingRetriedEvent(
                        job.getJobId(), job.getDocumentId(), job.getVersionId(), job.getTenantId(),
                        job.getProvider(), job.getModelName(), job.getModelVersion(), job.getRetryCount()
                ));
                // Resubmit with exponential backoff delay
                long delay = properties.retryBackoffMs() * (long) Math.pow(2, job.getRetryCount());
                taskExecutor.submit(() -> {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ignored) {}
                    executeEmbedding(jobId);
                });
            } else {
                job.transitionToFailed(t.getMessage());
                jobRepository.save(job);
                publishAfterCommit(new EmbeddingFailedEvent(
                        job.getJobId(), job.getDocumentId(), job.getVersionId(), job.getTenantId(),
                        job.getProvider(), job.getModelName(), job.getModelVersion(), t.getMessage()
                ));
            }
        }
    }

    private boolean isRetryable(Throwable t) {
        String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
        return msg.contains("timeout") || msg.contains("timed out") || msg.contains("temporary") || msg.contains("network") || msg.contains("connection refused");
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
