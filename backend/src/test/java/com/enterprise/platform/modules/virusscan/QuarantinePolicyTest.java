package com.enterprise.platform.modules.virusscan;

import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.domain.ScanJobStatus;
import com.enterprise.platform.modules.virusscan.provider.VirusScanResult;
import com.enterprise.platform.modules.virusscan.domain.VirusScanEvents.DocumentQuarantinedEvent;
import com.enterprise.platform.modules.virusscan.repository.ScanJobRepository;
import com.enterprise.platform.modules.virusscan.service.DefaultQuarantinePolicy;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QuarantinePolicyTest {

    @Test
    void testPolicyTransitionsAndSavesAggregate() {
        // GIVEN
        ScanJobRepository repository = mock(ScanJobRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        DefaultQuarantinePolicy policy = new DefaultQuarantinePolicy(repository, eventPublisher);

        ScanJob job = new ScanJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        job.transitionToScanning();
        job.transitionToInfected("ClamAV", "1.0", "Eicar", 45, 100);

        VirusScanResult result = new VirusScanResult(ScanJobStatus.INFECTED, "ClamAV", "1.0", "Eicar", Instant.now(), 45, 100);

        // WHEN
        policy.execute(job, result);

        // THEN
        assertEquals(ScanJobStatus.QUARANTINED, job.getStatus());
        verify(repository, times(1)).save(job);
        verify(eventPublisher, times(1)).publishEvent(any(DocumentQuarantinedEvent.class));
    }
}
