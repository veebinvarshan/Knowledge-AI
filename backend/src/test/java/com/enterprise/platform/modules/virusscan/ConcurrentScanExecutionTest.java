package com.enterprise.platform.modules.virusscan;

import com.enterprise.platform.core.config.properties.VirusScanProperties;
import com.enterprise.platform.modules.storage.repository.StorageObjectRepository;
import com.enterprise.platform.modules.storage.service.StorageService;
import com.enterprise.platform.modules.virusscan.provider.VirusScannerProviderResolver;
import com.enterprise.platform.modules.virusscan.repository.ScanJobRepository;
import com.enterprise.platform.modules.virusscan.service.QuarantinePolicy;
import com.enterprise.platform.modules.virusscan.service.VirusScanServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConcurrentScanExecutionTest {

    @Test
    void testExecutorConcurrentlyConfigured() {
        // GIVEN
        VirusScanProperties properties = new VirusScanProperties(true, "CLAMAV", 3, 1000, 5, 20, "QUARANTINE");

        // WHEN
        VirusScanServiceImpl service = new VirusScanServiceImpl(
                mock(ScanJobRepository.class),
                mock(StorageObjectRepository.class),
                mock(StorageService.class),
                mock(VirusScannerProviderResolver.class),
                mock(QuarantinePolicy.class),
                mock(ApplicationEventPublisher.class),
                properties
        );

        // THEN
        assertNotNull(service.getTaskExecutor());
        assertEquals(5, service.getTaskExecutor().getCorePoolSize());
        assertEquals(5, service.getTaskExecutor().getMaxPoolSize());
        // Clean shutdown
        service.getTaskExecutor().shutdown();
    }
}
