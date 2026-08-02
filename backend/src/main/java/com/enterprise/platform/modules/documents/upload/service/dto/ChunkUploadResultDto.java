package com.enterprise.platform.modules.documents.upload.service.dto;

import java.util.UUID;

public record ChunkUploadResultDto(
    UUID sessionId,
    int chunkNumber,
    boolean success,
    String message
) {}
