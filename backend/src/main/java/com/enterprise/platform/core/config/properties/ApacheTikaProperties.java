package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "platform.metadata.tika")
@Validated
public record ApacheTikaProperties(
    @Min(0) long maxMetadataSize,
    @Min(0) int maxEmbeddedDepth,
    @Min(0) int maxRecursionDepth,
    @Min(0) int parserTimeoutMs
) {}
