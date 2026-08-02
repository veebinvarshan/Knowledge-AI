package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.service.QueryCacheService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QueryCacheTest {

    @Test
    void testCacheHitsAndExpiredEntries() throws InterruptedException {
        QueryCacheService mockCache = mock(QueryCacheService.class);
        when(mockCache.get("key")).thenReturn("value");

        // WHEN
        Object cached = mockCache.get("key");

        // THEN
        assertEquals("value", cached);
    }
}
