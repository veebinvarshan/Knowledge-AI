package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.provider.EmbeddingProvider;
import com.enterprise.platform.modules.embedding.service.EmbeddingProviderResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProviderResolverTest {

    @Test
    void testProviderResolverResolvesCorrectlyByPriority() {
        EmbeddingProvider p1 = mock(EmbeddingProvider.class);
        EmbeddingProvider p2 = mock(EmbeddingProvider.class);

        when(p1.supports("GEMINI")).thenReturn(true);
        when(p1.getPriority()).thenReturn(2);

        when(p2.supports("GEMINI")).thenReturn(true);
        when(p2.getPriority()).thenReturn(1); // higher priority (lower value)

        EmbeddingProviderResolver resolver = new EmbeddingProviderResolver(List.of(p1, p2));

        // WHEN
        EmbeddingProvider resolved = resolver.resolve("GEMINI");

        // THEN
        assertEquals(p2, resolved);
    }
}
