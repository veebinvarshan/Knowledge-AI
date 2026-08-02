package com.enterprise.platform.modules.documents.upload.service;

import com.enterprise.platform.core.config.properties.StorageProperties;
import com.enterprise.platform.modules.documents.upload.exception.QuotaExceededException;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import org.springframework.stereotype.Service;

@Service
public class QuotaServiceImpl implements QuotaService {

    private final StorageObjectRepository storageObjectRepository;
    private final StorageProperties storageProperties;

    public QuotaServiceImpl(StorageObjectRepository storageObjectRepository, StorageProperties storageProperties) {
        this.storageObjectRepository = storageObjectRepository;
        this.storageProperties = storageProperties;
    }

    @Override
    public void validateUploadQuota(String tenantId, long proposedFileSize) throws QuotaExceededException {
        long used = getTenantUsedStorage(tenantId);
        long quota = storageProperties.tenantQuotaBytes();
        if (used + proposedFileSize > quota) {
            throw new QuotaExceededException("Upload rejected: tenant storage quota exceeded. Limit: " + quota + " bytes, Used: " + used + " bytes, Proposed: " + proposedFileSize + " bytes");
        }
    }

    @Override
    public long getTenantUsedStorage(String tenantId) {
        return storageObjectRepository.sumSizeBytesByTenantId(tenantId);
    }
}
