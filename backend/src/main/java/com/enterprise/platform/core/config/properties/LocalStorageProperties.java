package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.storage.local")
@Validated
public record LocalStorageProperties(
    @NotBlank String rootDirectory
) {}
