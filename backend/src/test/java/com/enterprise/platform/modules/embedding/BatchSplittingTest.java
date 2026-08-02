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

public class BatchSplittingTest {

    @Test
    void testBatchSplittingDividesLargeRequest() {
        EmbeddingModel mockModel = mock(EmbeddingModel.class);
        ObjectProvider<EmbeddingModel> mockProvider = mock(ObjectProvider.class);
        when(mockProvider.getIfAvailable()).thenReturn(mockModel);

        EmbeddingProperties embeddingProperties = new EmbeddingProperties(true, "GEMINI", "model", "v1", 3, 1000);
        // Set maxChunksPerBatch to 2
        GeminiEmbeddingProperties geminiProperties = new GeminiEmbeddingProperties("project", "us", 5000, 2);

        when(mockModel.embed(anyString())).thenReturn(new float[768]);

        GeminiEmbeddingProvider provider = new GeminiEmbeddingProvider(mockProvider, embeddingProperties, geminiProperties);

        // WHEN: Generate for 5 chunks
        EmbeddingResult result = provider.generate(List.of("1", "2", "3", "4", "5"));

        // THEN: Verify all 5 embeddings are returned and generated
        assertEquals(5, result.embeddings().size());
        verify(mockModel, times(5)).embed(anyString());
    }
}
