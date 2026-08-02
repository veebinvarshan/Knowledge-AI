package com.enterprise.platform.modules.search;

import com.enterprise.platform.modules.search.provider.SearchProvider;
import com.enterprise.platform.modules.search.service.SearchProviderResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProviderPriorityResolverTest {

    @Test
    void testResolverChoosesLowestOrderPriority() {
        SearchProvider hybridProvider = mock(SearchProvider.class);
        SearchProvider luceneProvider = mock(SearchProvider.class);
        SearchProvider qdrantProvider = mock(SearchProvider.class);

        // Priority 1
        when(hybridProvider.supports("HYBRID")).thenReturn(true);
        when(hybridProvider.getPriority()).thenReturn(1);

        // Priority 2
        when(luceneProvider.supports("HYBRID")).thenReturn(true);
        when(luceneProvider.getPriority()).thenReturn(2);

        // Priority 3
        when(qdrantProvider.supports("HYBRID")).thenReturn(true);
        when(qdrantProvider.getPriority()).thenReturn(3);

        SearchProviderResolver resolver = new SearchProviderResolver(
                List.of(qdrantProvider, luceneProvider, hybridProvider)
        );

        // WHEN
        SearchProvider resolved = resolver.resolve("HYBRID");

        // THEN
        assertEquals(hybridProvider, resolved);
    }
}
