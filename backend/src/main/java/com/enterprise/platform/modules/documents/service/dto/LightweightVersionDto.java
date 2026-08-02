package com.enterprise.platform.modules.documents.service.dto;

import java.time.Instant;
import java.util.UUID;

public record LightweightVersionDto(
    int versionNumber,
    String filename,
    String mimeType,
    long fileSize,
    String checksum,
    UUID uploadedBy,
    Instant uploadedAt,
    boolean currentVersion,
    String processingStatus,
    String storageProvider
) {}
