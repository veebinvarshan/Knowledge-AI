package com.enterprise.platform.infrastructure.ratelimit;

public interface RateLimiter {
    boolean isAllowed(String key, RateLimitPolicy policy);
    long getRetryAfterSeconds(String key, RateLimitPolicy policy);
}
