package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.core.config.properties.SemanticSearchProperties;
import com.enterprise.platform.modules.semanticsearch.service.QueryCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RedisQueryCacheTest {

    @Test
    void testRedisTemplateCachesHits() {
        ObjectProvider<RedisTemplate<String, Object>> mockProvider = mock(ObjectProvider.class);
        RedisTemplate<String, Object> mockTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> mockOps = mock(ValueOperations.class);

        when(mockProvider.getIfAvailable()).thenReturn(mockTemplate);
        when(mockTemplate.opsForValue()).thenReturn(mockOps);
        when(mockOps.get("key")).thenReturn("value");

        SemanticSearchProperties properties = new SemanticSearchProperties(true, "HYBRID", "QDRANT", "COSINE", 10, false, 500);
        QueryCacheService service = new QueryCacheService(mockProvider, properties);

        Object result = service.get("key");

        assertEquals("value", result);
        verify(mockOps).get("key");
    }
}
