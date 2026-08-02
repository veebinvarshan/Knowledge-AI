package com.enterprise.platform.infrastructure.ratelimit;

import com.enterprise.platform.core.config.properties.RateLimitingProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RateLimiterTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RateLimitingProperties properties;
    private RedisRateLimiter rateLimiter;
    private RateLimitKeyResolver keyResolver;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        properties = new RateLimitingProperties(5, 3, 2, 10, 60);
        rateLimiter = new RedisRateLimiter(redisTemplate, properties);
        keyResolver = new RateLimitKeyResolver();

        request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void testRateLimitAllowedBelowLimit() {
        // GIVEN
        String key = "test-key";
        RateLimitPolicy policy = RateLimitPolicy.AUTHENTICATION;
        String redisKey = "ratelimit:" + policy.name() + ":" + key;

        when(valueOperations.increment(redisKey)).thenReturn(3L);

        // WHEN
        boolean allowed = rateLimiter.isAllowed(key, policy);

        // THEN (Allowed since 3 <= limit of 5)
        assertTrue(allowed);
    }

    @Test
    void testRateLimitBlockedAboveLimit() {
        // GIVEN
        String key = "test-key";
        RateLimitPolicy policy = RateLimitPolicy.AUTHENTICATION;
        String redisKey = "ratelimit:" + policy.name() + ":" + key;

        when(valueOperations.increment(redisKey)).thenReturn(6L);

        // WHEN
        boolean allowed = rateLimiter.isAllowed(key, policy);

        // THEN (Blocked since 6 > limit of 5)
        assertFalse(allowed);
    }

    @Test
    void testRateLimiterKeyResolvers() {
        // GIVEN
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");

        // WHEN
        String ipKey = keyResolver.resolveByIp(request);
        String userKey = keyResolver.resolveByUser("usr-123", request);
        String tenantKey = keyResolver.resolveByTenant("tenant-abc", request);

        // THEN
        assertEquals("10.0.0.1", ipKey);
        assertEquals("user:usr-123", userKey);
        assertEquals("tenant:tenant-abc", tenantKey);
    }

    @Test
    void testRateLimiterFailOpenOnRedisConnectionExceptions() {
        // GIVEN
        String key = "test-key";
        RateLimitPolicy policy = RateLimitPolicy.AUTHENTICATION;
        String redisKey = "ratelimit:" + policy.name() + ":" + key;

        when(valueOperations.increment(redisKey)).thenThrow(new RuntimeException("Redis connection timed out"));

        // WHEN
        boolean allowed = rateLimiter.isAllowed(key, policy);

        // THEN (Fails open, returning true to prevent user lockouts)
        assertTrue(allowed);
    }
}
