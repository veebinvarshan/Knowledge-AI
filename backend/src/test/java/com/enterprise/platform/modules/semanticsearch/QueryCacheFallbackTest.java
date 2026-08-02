package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.core.config.properties.SemanticSearchProperties;
import com.enterprise.platform.modules.semanticsearch.service.QueryCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QueryCacheFallbackTest {

    @Test
    void testQueryCacheFallbackToLocalMap() {
        ObjectProvider<RedisTemplate<String, Object>> mockProvider = mock(ObjectProvider.class);
        // Returns null indicating Redis is unavailable
        when(mockProvider.getIfAvailable()).thenReturn(null);

        SemanticSearchProperties properties = new SemanticSearchProperties(true, "HYBRID", "QDRANT", "COSINE", 10, false, 500);
        QueryCacheService cacheService = new QueryCacheService(mockProvider, properties);

        // WHEN
        cacheService.put("key1", "cached_value");
        Object result = cacheService.get("key1");

        // THEN
        assertEquals("cached_value", result);
    }
}
