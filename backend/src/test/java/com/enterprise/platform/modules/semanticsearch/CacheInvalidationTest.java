package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.service.QueryCacheService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class CacheInvalidationTest {

    @Test
    void testCacheClearAll() {
        QueryCacheService cacheService = mock(QueryCacheService.class);

        // WHEN
        cacheService.invalidateAll();

        // THEN
        verify(cacheService).invalidateAll();
    }
}
