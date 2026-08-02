package com.enterprise.platform.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "platform.virusscan.clamav")
@Validated
public record ClamAvProperties(
    @NotBlank String host,
    @Min(1) int port,
    @Min(0) int connectionTimeoutMs,
    @Min(0) int readTimeoutMs
) {}
