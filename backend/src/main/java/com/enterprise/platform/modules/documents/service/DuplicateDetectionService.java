package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.service.dto.DuplicateDetectionResult;
import java.util.UUID;

public interface DuplicateDetectionService {
    DuplicateDetectionResult evaluateDuplicate(String tenantId, String checksum, String fileName, UUID userId);
}
