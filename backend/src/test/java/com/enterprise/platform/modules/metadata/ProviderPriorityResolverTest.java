package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.modules.metadata.provider.MetadataExtractor;
import com.enterprise.platform.modules.metadata.service.MetadataProviderResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProviderPriorityResolverTest {

    @Test
    void testResolverChoosesLowestOrderPriority() {
        // GIVEN
        MetadataExtractor explicitExtractor = mock(MetadataExtractor.class);
        MetadataExtractor tikaGenericExtractor = mock(MetadataExtractor.class);
        MetadataExtractor fallbackExtractor = mock(MetadataExtractor.class);

        // Priority 1
        when(explicitExtractor.supports("application/pdf")).thenReturn(true);
        when(explicitExtractor.getPriority()).thenReturn(1);

        // Priority 2
        when(tikaGenericExtractor.supports("application/pdf")).thenReturn(true);
        when(tikaGenericExtractor.getPriority()).thenReturn(2);

        // Priority 100
        when(fallbackExtractor.supports("application/pdf")).thenReturn(true);
        when(fallbackExtractor.getPriority()).thenReturn(100);

        MetadataProviderResolver resolver = new MetadataProviderResolver(
                List.of(fallbackExtractor, explicitExtractor, tikaGenericExtractor)
        );

        // WHEN
        MetadataExtractor resolved = resolver.resolve("application/pdf");

        // THEN (Resolves the lowest order value, i.e. 1)
        assertEquals(explicitExtractor, resolved);
    }
}
