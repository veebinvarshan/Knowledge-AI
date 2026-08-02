package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.storage.s3")
@Validated
public record S3StorageProperties(
    @NotBlank String bucket,
    @NotBlank String region,
    String accessKey,
    String secretKey
) {}
