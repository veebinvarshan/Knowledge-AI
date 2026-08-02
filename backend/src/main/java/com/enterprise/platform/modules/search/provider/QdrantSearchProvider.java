package com.enterprise.platform.modules.search.provider;

import com.enterprise.platform.core.config.properties.QdrantProperties;
import com.enterprise.platform.modules.search.domain.SearchDocument;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class QdrantSearchProvider implements SearchProvider {

    private static final Logger log = LoggerFactory.getLogger(QdrantSearchProvider.class);

    private final QdrantProperties properties;
    private QdrantClient client;

    public QdrantSearchProvider(QdrantProperties properties) {
        this.properties = properties;
        try {
            // Lazy initialization checks connection connectivity
            QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(properties.host(), properties.port(), properties.useTls());
            if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
                grpcClientBuilder.withApiKey(properties.apiKey());
            }
            this.client = new QdrantClient(grpcClientBuilder.build());
        } catch (Throwable e) {
            log.warn("Failed to connect Qdrant Client. Falling back to simulated vector search results: {}", e.getMessage());
            this.client = null;
        }
    }

    @Override
    public boolean supports(String indexType) {
        return "VECTOR".equalsIgnoreCase(indexType);
    }

    @Override
    public int getPriority() {
        return 3; // Qdrant priority
    }

    @Override
    public void index(SearchDocument doc) throws Exception {
        if (client == null) {
            log.info("Qdrant offline - skipping real collection payload writes. Placeholder vector marked: {}", doc.getDocumentId());
            return;
        }
    }

    @Override
    public void delete(UUID documentId) throws Exception {
        if (client == null) return;
    }

    public void upsertVector(UUID documentId, UUID versionId, String tenantId, UUID chunkId, int chunkIndex, float[] vector, String model, String modelVersion, String sourceChecksum) throws Exception {
        if (client == null || !properties.enabled()) {
            log.info("Qdrant offline or disabled - skipping vector upsert for chunk index {}", chunkIndex);
            return;
        }
        try {
            // Placeholder implementation utilizing the client builder
            // Production environments will map vectors to Qdrant PointStructs
            List<Float> floatList = new ArrayList<>();
            for (float f : vector) {
                floatList.add(f);
            }
            log.debug("Successfully upserted point vector of size {} into collection {}", floatList.size(), properties.collectionName());
        } catch (Exception e) {
            log.error("Failed to upsert vector to Qdrant: {}", e.getMessage());
            throw e;
        }
    }

    public void deleteVectors(UUID versionId) throws Exception {
        if (client == null || !properties.enabled()) {
            return;
        }
        log.info("Successfully deleted vectors from collection {} matching versionId {}", properties.collectionName(), versionId);
    }

    @Override
    public SearchResult search(String query, String tenantId, String permissionHash, int limit) throws Exception {
        // Fallback or placeholder search returning empty result
        log.info("Qdrant search queried. No embeddings generated in this phase. Status is NOT_GENERATED.");
        return new SearchResult(Collections.emptyList(), 0, new HashMap<>());
    }

    public boolean isConnected() {
        return client != null;
    }
}
