package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.storage")
@Validated
public record StorageProperties(
    @NotBlank String provider,
    @Min(1) long tenantQuotaBytes
) {}
