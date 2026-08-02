package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.api")
@Validated
public record ApiVersionProperties(
    @NotBlank String defaultVersion,
    @NotBlank String basePrefix
) {}
