package com.enterprise.platform.modules.ocr.service;

import com.enterprise.platform.modules.ocr.domain.OcrJob;
import java.util.UUID;

public interface OcrService {
    OcrJob submitOcrJob(String tenantId, UUID userId, UUID documentId, UUID versionId, UUID storageObjectId);
    void executeOcr(UUID jobId, String tenantId, UUID userId);
}
