package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.core.config.properties.QueryEmbeddingProperties;
import com.enterprise.platform.modules.embedding.provider.EmbeddingProvider;
import com.enterprise.platform.modules.embedding.provider.EmbeddingResult;
import com.enterprise.platform.modules.embedding.service.EmbeddingProviderResolver;
import com.enterprise.platform.modules.semanticsearch.service.QueryEmbeddingService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QueryEmbeddingServiceTest {

    @Test
    void testQueryEmbeddingGeneratesAndDoesNotPersist() {
        EmbeddingProviderResolver resolver = mock(EmbeddingProviderResolver.class);
        EmbeddingProvider mockProvider = mock(EmbeddingProvider.class);

        EmbeddingProperties properties1 = new EmbeddingProperties(true, "GEMINI", "model", "v1", 3, 1000);
        QueryEmbeddingProperties properties2 = new QueryEmbeddingProperties("model", "v1", 768);

        when(resolver.resolve("GEMINI")).thenReturn(mockProvider);
        when(mockProvider.generate(List.of("query"))).thenReturn(
                new EmbeddingResult(List.of(new float[768]), 768, "model", "v1")
        );

        QueryEmbeddingService service = new QueryEmbeddingService(resolver, properties1, properties2);

        // WHEN
        float[] vector = service.generateQueryVector("query");

        // THEN: generates vector
        assertNotNull(vector);
        assertEquals(768, vector.length);
    }
}
