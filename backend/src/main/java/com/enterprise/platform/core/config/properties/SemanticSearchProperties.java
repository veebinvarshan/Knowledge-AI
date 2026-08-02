package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "platform.semanticsearch")
@Validated
public record SemanticSearchProperties(
    boolean enabled,
    @NotBlank String mode, // SEMANTIC_ONLY, LEXICAL_ONLY, HYBRID
    @NotBlank String provider,
    @NotBlank String similarityMetric,
    @Min(1) int defaultLimit,
    boolean logRawQueries,
    @Min(1) long cacheTtlMs
) {}
