package com.enterprise.platform.modules.search.provider;

import com.enterprise.platform.core.config.properties.SearchProperties;
import com.enterprise.platform.modules.search.domain.SearchDocument;
import com.enterprise.platform.modules.search.service.HybridRankingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "platform.search.qdrant.enabled", havingValue = "true", matchIfMissing = true)
public class HybridSearchProvider implements SearchProvider {

    private final LuceneSearchProvider luceneProvider;
    private final QdrantSearchProvider qdrantProvider;
    private final SearchProperties properties;
    private final HybridRankingService rankingService;

    public HybridSearchProvider(
            LuceneSearchProvider luceneProvider,
            QdrantSearchProvider qdrantProvider,
            SearchProperties properties,
            HybridRankingService rankingService) {
        this.luceneProvider = luceneProvider;
        this.qdrantProvider = qdrantProvider;
        this.properties = properties;
        this.rankingService = rankingService;
    }

    @Override
    public boolean supports(String indexType) {
        return "HYBRID".equalsIgnoreCase(indexType);
    }

    @Override
    public int getPriority() {
        return 1; // Primary hybrid search provider
    }

    @Override
    public void index(SearchDocument doc) throws Exception {
        luceneProvider.index(doc);
        qdrantProvider.index(doc);
    }

    @Override
    public void delete(UUID documentId) throws Exception {
        luceneProvider.delete(documentId);
        qdrantProvider.delete(documentId);
    }

    @Override
    public SearchResult search(String query, String tenantId, String permissionHash, int limit) throws Exception {
        // Run BM25 search
        SearchResult bm25Result = luceneProvider.search(query, tenantId, permissionHash, limit);
        
        // Run Vector search
        SearchResult vectorResult = qdrantProvider.search(query, tenantId, permissionHash, limit);

        // Merge using Reciprocal Rank Fusion (RRF) delegated ranking service
        return rankingService.performRrf(
                bm25Result,
                vectorResult,
                properties.bm25Weight(),
                properties.vectorWeight(),
                properties.rrfConstant(),
                limit
        );
    }
}
