package com.enterprise.platform.modules.storage.service.dto;

public record StorageLocation(
    String providerId,
    String logicalPath,
    String providerObjectKey
) {}
