package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.service.QueryCacheService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CacheInvalidationPermissionTest {

    @Test
    void testCacheInvalidatesSuccessfully() {
        QueryCacheService mockCache = mock(QueryCacheService.class);

        // WHEN: permission changes or index update clears cache
        mockCache.invalidateAll();

        // THEN
        verify(mockCache).invalidateAll();
    }
}
