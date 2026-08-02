package com.enterprise.platform.modules.documents.upload.service.dto;

import com.enterprise.platform.modules.documents.upload.domain.UploadSessionStatus;
import java.time.Instant;
import java.util.UUID;

public record UploadSessionDto(
    UUID id,
    String fileName,
    long fileSizeBytes,
    String mimeType,
    UploadSessionStatus status,
    int chunksTotal,
    Instant expiresAt,
    Instant createdAt
) {}
