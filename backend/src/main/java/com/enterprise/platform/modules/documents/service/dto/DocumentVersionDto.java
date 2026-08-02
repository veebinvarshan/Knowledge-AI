package com.enterprise.platform.modules.documents.service.dto;

import com.enterprise.platform.modules.documents.domain.VersionStatus;
import com.enterprise.platform.modules.documents.domain.VersionType;
import java.time.Instant;
import java.util.UUID;

public record DocumentVersionDto(
    UUID id,
    UUID documentId,
    int versionNumber,
    UUID storageObjectId,
    String checksum,
    String checksumAlgorithm,
    long sizeBytes,
    String mimeType,
    UUID createdBy,
    Instant createdAt,
    VersionType versionType,
    VersionStatus status,
    String comment
) {}
