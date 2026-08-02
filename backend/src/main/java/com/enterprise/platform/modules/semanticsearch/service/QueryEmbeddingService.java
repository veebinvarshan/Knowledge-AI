package com.enterprise.platform.modules.semanticsearch.service;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.core.config.properties.QueryEmbeddingProperties;
import com.enterprise.platform.modules.embedding.provider.EmbeddingProvider;
import com.enterprise.platform.modules.embedding.provider.EmbeddingResult;
import com.enterprise.platform.modules.embedding.service.EmbeddingProviderResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryEmbeddingService {

    private final EmbeddingProviderResolver resolver;
    private final EmbeddingProperties embeddingProperties;
    private final QueryEmbeddingProperties queryProperties;

    public QueryEmbeddingService(
            EmbeddingProviderResolver resolver,
            EmbeddingProperties embeddingProperties,
            QueryEmbeddingProperties queryProperties) {
        this.resolver = resolver;
        this.embeddingProperties = embeddingProperties;
        this.queryProperties = queryProperties;
    }

    public float[] generateQueryVector(String queryText) {
        // Model consistency check (Refinement 3)
        verifyModelConsistency();

        EmbeddingProvider provider = resolver.resolve(embeddingProperties.provider());
        EmbeddingResult result = provider.generate(List.of(queryText));

        if (result.embeddings().isEmpty()) {
            throw new IllegalStateException("Failed to generate embedding vector for query.");
        }

        return result.embeddings().get(0);
    }

    public void verifyModelConsistency() {
        if (!embeddingProperties.modelName().equals(queryProperties.modelName())
                || !embeddingProperties.modelVersion().equals(queryProperties.modelVersion())) {
            throw new IllegalArgumentException("Embedding model mismatch! Document model: " 
                    + embeddingProperties.modelName() + "-" + embeddingProperties.modelVersion() 
                    + ", Query model: " + queryProperties.modelName() + "-" + queryProperties.modelVersion());
        }
    }
}
