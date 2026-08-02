package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "platform.metadata")
@Validated
public record MetadataProperties(
    boolean enabled,
    @NotBlank String provider,
    @Min(0) int retryCount,
    @Min(1) long retryBackoffMs
) {}
