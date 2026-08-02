package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.storage.minio")
@Validated
public record MinioStorageProperties(
    @NotBlank String endpoint,
    @NotBlank String bucket,
    String accessKey,
    String secretKey
) {}
