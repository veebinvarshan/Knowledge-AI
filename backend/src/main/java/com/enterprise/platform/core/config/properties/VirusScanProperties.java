package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "platform.virusscan")
@Validated
public record VirusScanProperties(
    boolean enabled,
    @NotBlank String provider,
    @Min(0) int retryCount,
    @Min(1) long retryBackoffMs,
    @Min(1) int workerThreads,
    @Min(1) int queueCapacity,
    @NotBlank String quarantinePolicy
) {}
