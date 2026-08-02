package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.core.config.properties.QueryEmbeddingProperties;
import com.enterprise.platform.modules.embedding.service.EmbeddingProviderResolver;
import com.enterprise.platform.modules.semanticsearch.service.QueryEmbeddingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmbeddingModelConsistencyTest {

    @Test
    void testMismatchThrowsException() {
        EmbeddingProviderResolver resolver = mock(EmbeddingProviderResolver.class);
        EmbeddingProperties properties1 = new EmbeddingProperties(true, "GEMINI", "model-A", "v1", 3, 1000);
        QueryEmbeddingProperties properties2 = new QueryEmbeddingProperties("model-B", "v1", 768);

        QueryEmbeddingService service = new QueryEmbeddingService(resolver, properties1, properties2);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, service::verifyModelConsistency);
    }
}
