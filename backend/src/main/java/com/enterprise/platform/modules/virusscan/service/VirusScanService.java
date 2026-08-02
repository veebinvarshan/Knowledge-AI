package com.enterprise.platform.modules.virusscan.service;

import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import java.util.UUID;

public interface VirusScanService {
    ScanJob submitScanJob(String tenantId, UUID userId, UUID documentId, UUID versionId, UUID storageObjectId);
    void executeScan(UUID scanJobId, String tenantId, UUID userId);
    boolean isQuarantined(UUID versionId);
}
