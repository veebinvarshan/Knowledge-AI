package com.enterprise.platform.modules.documents.upload.service.dto;

import com.enterprise.platform.modules.documents.upload.domain.UploadSessionStatus;
import java.util.List;
import java.util.UUID;

public record ResumeStatusDto(
    UUID sessionId,
    UploadSessionStatus status,
    List<Integer> uploadedChunks,
    int chunksTotal,
    long fileSizeBytes
) {}
