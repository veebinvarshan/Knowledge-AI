package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.jwt")
@Validated
public record JwtProperties(
    @NotBlank String secret,
    @Min(60) long expirationSeconds
) {}
