package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "platform.search.qdrant")
@Validated
public record QdrantProperties(
    boolean enabled,
    @NotBlank String host,
    @Min(1) int port,
    @Min(1) int connectionTimeoutMs,
    boolean useTls,
    String apiKey,
    @NotBlank String collectionName
) {}
