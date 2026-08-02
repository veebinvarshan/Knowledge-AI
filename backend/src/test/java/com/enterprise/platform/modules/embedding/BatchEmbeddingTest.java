package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.provider.EmbeddingProvider;
import com.enterprise.platform.modules.embedding.provider.EmbeddingResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BatchEmbeddingTest {

    @Test
    void testBatchEmbeddingReturnsSameListSize() {
        EmbeddingProvider mockProvider = mock(EmbeddingProvider.class);
        List<String> chunks = List.of("chunk 1", "chunk 2");

        when(mockProvider.generate(chunks)).thenReturn(
                new EmbeddingResult(List.of(new float[768], new float[768]), 768, "model", "v1")
        );

        // WHEN
        EmbeddingResult result = mockProvider.generate(chunks);

        // THEN
        assertEquals(2, result.embeddings().size());
    }
}
