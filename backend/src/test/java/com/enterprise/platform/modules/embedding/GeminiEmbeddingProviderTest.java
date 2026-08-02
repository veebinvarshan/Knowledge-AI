package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.core.config.properties.GeminiEmbeddingProperties;
import com.enterprise.platform.modules.embedding.provider.GeminiEmbeddingProvider;
import com.enterprise.platform.modules.embedding.provider.EmbeddingResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GeminiEmbeddingProviderTest {

    @Test
    void testGeminiProviderGeneratesVectorsWithExpectedDimensions() {
        ObjectProvider<EmbeddingModel> mockProvider = mock(ObjectProvider.class);
        EmbeddingProperties embeddingProperties = new EmbeddingProperties(true, "GEMINI", "model", "v1", 3, 1000);
        GeminiEmbeddingProperties geminiProperties = new GeminiEmbeddingProperties("project", "us", 5000, 30);

        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(mockProvider, embeddingProperties, geminiProperties);

        // WHEN
        EmbeddingResult result = provider.generate(List.of("text chunk 1"));

        // THEN
        assertEquals(1, result.embeddings().size());
        assertEquals(768, result.dimensions());
        assertEquals("model", result.modelName());
    }
}
