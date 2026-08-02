package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "platform.embedding.gemini")
@Validated
public record GeminiEmbeddingProperties(
    @NotBlank String projectId,
    @NotBlank String location,
    @Min(1) int connectionTimeoutMs,
    @Min(1) int maxChunksPerBatch
) {}
