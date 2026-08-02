package com.enterprise.platform.modules.metadata.service;

import com.enterprise.platform.modules.metadata.domain.MetadataJob;
import java.util.UUID;

public interface MetadataExtractionService {
    MetadataJob submitExtractionJob(String tenantId, UUID userId, UUID documentId, UUID versionId, UUID storageObjectId);
    void executeExtraction(UUID jobId, String tenantId, UUID userId);
}
