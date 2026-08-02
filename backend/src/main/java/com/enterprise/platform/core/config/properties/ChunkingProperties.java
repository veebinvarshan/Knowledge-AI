package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "platform.embedding.chunking")
@Validated
public record ChunkingProperties(
    @NotBlank String strategy,
    @Min(1) int maxCharacters,
    @Min(0) int overlapCharacters
) {}
