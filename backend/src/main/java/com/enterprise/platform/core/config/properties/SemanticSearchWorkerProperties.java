package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "platform.semanticsearch.worker")
@Validated
public record SemanticSearchWorkerProperties(
    @Min(1) int threads,
    @Min(1) int queueCapacity
) {}
