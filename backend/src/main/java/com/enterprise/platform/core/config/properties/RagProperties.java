package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "platform.rag")
@Validated
public record RagProperties(
    boolean enabled,
    @NotBlank String defaultModelName,
    @Min(1) int defaultMaxContextTokens,
    @jakarta.validation.constraints.DecimalMin("0.0") @jakarta.validation.constraints.DecimalMax("2.0") double defaultTemperature,
    boolean enforceHallucinationSafeguards,
    @NotBlank String systemPromptTemplate
) {}
