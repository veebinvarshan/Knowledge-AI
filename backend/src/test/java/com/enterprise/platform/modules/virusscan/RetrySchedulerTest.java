package com.enterprise.platform.modules.virusscan;

import com.enterprise.platform.core.config.properties.VirusScanProperties;
import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.domain.ScanJobStatus;
import com.enterprise.platform.modules.virusscan.repository.ScanJobRepository;
import com.enterprise.platform.modules.virusscan.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class RetrySchedulerTest {

    private ScanJobRepository repository;
    private VirusScanService scanService;
    private VirusScanProperties properties;
    private RetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        repository = mock(ScanJobRepository.class);
        scanService = mock(VirusScanService.class);
        properties = new VirusScanProperties(true, "CLAMAV", 3, 1000, 2, 10, "QUARANTINE");
        scheduler = new RetryScheduler(repository, scanService, properties);
    }

    @Test
    void testSchedulerPicksAndExecutesRetries() {
        // GIVEN
        ScanJob failedJob = new ScanJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        failedJob.transitionToScanning();
        failedJob.transitionToFailed();
        failedJob.setRetryCount(1); // retry count is 1 (max retry count is 3)

        when(repository.findAllByStatusInAndNextRetryAtBefore(anyList(), any(Instant.class)))
                .thenReturn(List.of(failedJob));

        // WHEN
        scheduler.processPendingRetries();

        // THEN
        verify(scanService, times(1)).executeScan(eq(failedJob.getId()), eq("tenant-placeholder"), eq(null));
    }
}
