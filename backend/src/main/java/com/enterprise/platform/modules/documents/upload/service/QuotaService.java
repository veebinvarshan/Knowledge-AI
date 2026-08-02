package com.enterprise.platform.modules.documents.upload.service;

import com.enterprise.platform.modules.documents.upload.exception.QuotaExceededException;

public interface QuotaService {
    void validateUploadQuota(String tenantId, long proposedFileSize) throws QuotaExceededException;
    long getTenantUsedStorage(String tenantId);
}
