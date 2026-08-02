package com.enterprise.platform.core.config.properties;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.upload")
@Validated
public record UploadProperties(
    @Min(1024) long maxFileSize,
    @Min(1024) long maxChunkSize,
    @Min(1024) long minChunkSize,
    @Min(1) int maxChunksPerUpload,
    @Min(1) int maxConcurrentUploadsPerUser,
    @Min(1) int maxConcurrentUploadsPerTenant
) {}
