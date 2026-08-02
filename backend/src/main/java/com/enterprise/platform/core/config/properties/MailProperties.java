package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.mail")
@Validated
public record MailProperties(
    @NotBlank String host,
    @Min(1) @Max(65535) int port,
    String username,
    String password,
    String fromAddress,
    @Min(100) int connectionTimeoutMs
) {}
