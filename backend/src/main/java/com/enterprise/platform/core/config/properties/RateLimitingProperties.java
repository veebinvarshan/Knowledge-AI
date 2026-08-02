package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.ratelimit")
@Validated
public record RateLimitingProperties(
    @Min(1) int limitAuthentication,
    @Min(1) int limitPasswordReset,
    @Min(1) int limitEmailVerification,
    @Min(1) int limitFutureAi,
    @Min(1) long windowSeconds
) {}
