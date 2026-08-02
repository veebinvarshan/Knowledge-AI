package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.core.config.properties.SemanticSearchProperties;
import com.enterprise.platform.modules.semanticsearch.service.QueryCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RedisFallbackCacheTest {

    @Test
    void testRedisThrowsExceptionFallsBackToMemory() {
        ObjectProvider<RedisTemplate<String, Object>> mockProvider = mock(ObjectProvider.class);
        RedisTemplate<String, Object> mockTemplate = mock(RedisTemplate.class);

        when(mockProvider.getIfAvailable()).thenReturn(mockTemplate);
        // Simulate Redis Connection failure exception
        when(mockTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Redis offline"));

        SemanticSearchProperties properties = new SemanticSearchProperties(true, "HYBRID", "QDRANT", "COSINE", 10, false, 500);
        QueryCacheService service = new QueryCacheService(mockProvider, properties);

        // WHEN: Redis template call fails during caching
        service.put("key-redis-fail", "success_value");
        Object result = service.get("key-redis-fail");

        // THEN: Local cache should rescue and return value
        assertEquals("success_value", result);
    }
}
