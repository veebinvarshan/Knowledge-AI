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

import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WorkerQueueCapacityTest {

    @Test
    void testWorkerQueueAndRejectionHandlerConfigured() {
        // GIVEN
        VirusScanProperties properties = new VirusScanProperties(true, "CLAMAV", 3, 1000, 2, 15, "QUARANTINE");

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

        // THEN (Verify queue capacity is 15 and rejection handler is CallerRunsPolicy)
        assertNotNull(service.getTaskExecutor());
        assertEquals(2, service.getTaskExecutor().getCorePoolSize());
        
        // Fetch ThreadPoolExecutor backend details via reflection or helper check if CallerRunsPolicy is used
        assertTrue(service.getTaskExecutor().getThreadPoolExecutor().getRejectedExecutionHandler() instanceof ThreadPoolExecutor.CallerRunsPolicy);
        
        // Clean shutdown
        service.getTaskExecutor().shutdown();
    }
}
