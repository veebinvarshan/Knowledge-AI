package com.enterprise.platform.modules.storage.service.dto;

public record StorageResult(
    boolean success,
    StorageLocation location,
    StorageMetadata metadata
) {}
