package com.enterprise.platform.modules.storage.service.dto;

public record StorageMetadata(
    long sizeBytes,
    String mimeType,
    String checksum,
    String checksumAlgorithm
) {}
