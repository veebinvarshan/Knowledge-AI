package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "platform.ocr.tesseract")
@Validated
public record TesseractProperties(
    @NotBlank String tessdataPath,
    @Min(1) int parserTimeoutMs,
    @Min(1) int maxPages,
    @Min(1) int maxImageResolutionDpi
) {}
