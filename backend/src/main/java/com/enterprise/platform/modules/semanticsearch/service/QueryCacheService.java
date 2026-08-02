package com.enterprise.platform.modules.semanticsearch.service;

import com.enterprise.platform.core.config.properties.SemanticSearchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class QueryCacheService {

    private static final Logger log = LoggerFactory.getLogger(QueryCacheService.class);

    private final ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;
    private final SemanticSearchProperties properties;

    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    public QueryCacheService(
            ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider,
            SemanticSearchProperties properties) {
        this.redisTemplateProvider = redisTemplateProvider;
        this.properties = properties;
    }

    public Object get(String key) {
        try {
            RedisTemplate<String, Object> redis = redisTemplateProvider.getIfAvailable();
            if (redis != null) {
                return redis.opsForValue().get(key);
            }
        } catch (Exception e) {
            log.warn("Redis is unavailable; falling back to local in-memory cache. Error: {}", e.getMessage());
        }

        CacheEntry entry = localCache.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                localCache.remove(key);
                return null;
            }
            return entry.value();
        }
        return null;
    }

    public void put(String key, Object value) {
        try {
            RedisTemplate<String, Object> redis = redisTemplateProvider.getIfAvailable();
            if (redis != null) {
                redis.opsForValue().set(key, value, properties.cacheTtlMs(), TimeUnit.MILLISECONDS);
                return;
            }
        } catch (Exception e) {
            log.warn("Redis write failed; writing to local in-memory cache. Error: {}", e.getMessage());
        }

        long expiresAt = System.currentTimeMillis() + properties.cacheTtlMs();
        localCache.put(key, new CacheEntry(value, expiresAt));
    }

    public void invalidateAll() {
        try {
            RedisTemplate<String, Object> redis = redisTemplateProvider.getIfAvailable();
            if (redis != null) {
                redis.getConnectionFactory().getConnection().serverCommands().flushDb();
            }
        } catch (Exception e) {
            log.warn("Failed to flush Redis on invalidation: {}", e.getMessage());
        }
        localCache.clear();
        log.info("Query cache invalidated successfully.");
    }

    private static record CacheEntry(
        Object value,
        long expiresAt
    ) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
