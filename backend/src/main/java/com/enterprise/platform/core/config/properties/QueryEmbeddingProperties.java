package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "platform.semanticsearch.queryembedding")
@Validated
public record QueryEmbeddingProperties(
    @NotBlank String modelName,
    @NotBlank String modelVersion,
    @Min(1) int dimensions
) {}
