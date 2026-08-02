package com.enterprise.platform.infrastructure.ratelimit;

import com.enterprise.platform.core.config.properties.RateLimitingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);

    private final StringRedisTemplate redisTemplate;
    private final RateLimitingProperties properties;

    public RedisRateLimiter(StringRedisTemplate redisTemplate, RateLimitingProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public boolean isAllowed(String key, RateLimitPolicy policy) {
        try {
            int limit = getLimit(policy);
            long window = properties.windowSeconds();
            String redisKey = "ratelimit:" + policy.name() + ":" + key;

            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, window, TimeUnit.SECONDS);
            }

            return count != null && count <= limit;
        } catch (Exception e) {
            log.error("Redis connection failed during rate limit check for policy {}, key {}. Fail-safe open allowed.", policy, key, e);
            return true;
        }
    }

    @Override
    public long getRetryAfterSeconds(String key, RateLimitPolicy policy) {
        try {
            String redisKey = "ratelimit:" + policy.name() + ":" + key;
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : properties.windowSeconds();
        } catch (Exception e) {
            return properties.windowSeconds();
        }
    }

    private int getLimit(RateLimitPolicy policy) {
        return switch (policy) {
            case AUTHENTICATION -> properties.limitAuthentication();
            case PASSWORD_RESET -> properties.limitPasswordReset();
            case EMAIL_VERIFICATION -> properties.limitEmailVerification();
            case FUTURE_AI -> properties.limitFutureAi();
        };
    }
}
