package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "platform.ocr")
@Validated
public record OcrProperties(
    boolean enabled,
    @NotBlank String provider,
    @NotBlank String languages,
    @Min(0) int retryCount,
    @Min(1) long retryBackoffMs,
    @Min(0) double confidenceThreshold
) {}
