package com.enterprise.platform.modules.storage.service.dto;

public record StorageHealth(
    String providerId,
    String status,
    String message
) {}
