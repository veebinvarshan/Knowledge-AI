package com.enterprise.platform.modules.documents.upload;

import com.enterprise.platform.core.config.properties.StorageProperties;
import com.enterprise.platform.modules.documents.upload.exception.QuotaExceededException;
import com.enterprise.platform.modules.documents.upload.service.QuotaServiceImpl;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QuotaServiceTest {

    private StorageObjectRepository storageObjectRepository;
    private StorageProperties storageProperties;
    private QuotaServiceImpl quotaService;

    @BeforeEach
    void setUp() {
        storageObjectRepository = mock(StorageObjectRepository.class);
        storageProperties = mock(StorageProperties.class);
        quotaService = new QuotaServiceImpl(storageObjectRepository, storageProperties);
    }

    @Test
    void testQuotaValidPasses() {
        // GIVEN
        when(storageProperties.tenantQuotaBytes()).thenReturn(100L);
        when(storageObjectRepository.sumSizeBytesByTenantId("tenant-1")).thenReturn(30L);

        // WHEN / THEN (No exception should be thrown since 30 + 50 <= 100)
        assertDoesNotThrow(() ->
                quotaService.validateUploadQuota("tenant-1", 50L)
        );
    }

    @Test
    void testQuotaExceededBlocks() {
        // GIVEN
        when(storageProperties.tenantQuotaBytes()).thenReturn(100L);
        when(storageObjectRepository.sumSizeBytesByTenantId("tenant-1")).thenReturn(80L);

        // WHEN / THEN
        assertThrows(QuotaExceededException.class, () ->
                quotaService.validateUploadQuota("tenant-1", 30L)
        );
    }
}
