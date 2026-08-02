package com.enterprise.platform.modules.authorization.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermissionCacheService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCacheService.class);
    private static final String CACHE_KEY_PREFIX = "auth:perms:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    // Graceful fallback in case Redis connection fails or in test environment
    private final Map<String, Set<String>> localCache = new ConcurrentHashMap<>();

    public PermissionCacheService(Optional<StringRedisTemplate> redisTemplate) {
        this.redisTemplate = redisTemplate.orElse(null);
        if (this.redisTemplate == null) {
            log.warn("RedisTemplate is not configured. Falling back to local in-memory caching.");
        }
    }

    private String getCacheKey(UUID identityId, String tenantId) {
        return CACHE_KEY_PREFIX + tenantId + ":" + identityId.toString();
    }

    public Set<String> getUserPermissions(UUID identityId, String tenantId) {
        String key = getCacheKey(identityId, tenantId);
        if (redisTemplate != null) {
            try {
                List<String> cached = redisTemplate.opsForList().range(key, 0, -1);
                if (cached != null && !cached.isEmpty()) {
                    log.debug("Cache hit in Redis for user {}", identityId);
                    return new HashSet<>(cached);
                }
            } catch (Exception e) {
                log.warn("Redis read failure, resorting to local fallback: {}", e.getMessage());
            }
        }
        
        Set<String> localVal = localCache.get(key);
        if (localVal != null) {
            log.debug("Cache hit in local fallback for user {}", identityId);
            return localVal;
        }
        return null;
    }

    public void cacheUserPermissions(UUID identityId, String tenantId, Set<String> permissions) {
        String key = getCacheKey(identityId, tenantId);
        
        // Cache locally first
        localCache.put(key, permissions);

        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
                if (!permissions.isEmpty()) {
                    redisTemplate.opsForList().rightPushAll(key, permissions.toArray(new String[0]));
                    redisTemplate.expire(key, CACHE_TTL);
                }
                log.debug("Cached permissions in Redis for user {}", identityId);
            } catch (Exception e) {
                log.warn("Redis write failure: {}", e.getMessage());
            }
        }
    }

    public void evictUserPermissions(UUID identityId, String tenantId) {
        String key = getCacheKey(identityId, tenantId);
        localCache.remove(key);
        
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
                log.debug("Evicted permissions cache in Redis for user {}", identityId);
            } catch (Exception e) {
                log.warn("Redis delete failure: {}", e.getMessage());
            }
        }
    }

    public void evictAllPermissions() {
        localCache.clear();
        if (redisTemplate != null) {
            try {
                Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.debug("Evicted all permissions keys from Redis cache");
                }
            } catch (Exception e) {
                log.warn("Redis flush failure: {}", e.getMessage());
            }
        }
    }
}
