package com.enterprise.platform.modules.virusscan.service;

import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.domain.VirusScanEvents.DocumentQuarantinedEvent;
import com.enterprise.platform.modules.virusscan.provider.VirusScanResult;
import com.enterprise.platform.modules.virusscan.repository.ScanJobRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DefaultQuarantinePolicy implements QuarantinePolicy {

    private final ScanJobRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public DefaultQuarantinePolicy(ScanJobRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void execute(ScanJob scanJob, VirusScanResult result) {
        scanJob.transitionToQuarantined();
        repository.save(scanJob);

        // Publish DocumentQuarantinedEvent
        eventPublisher.publishEvent(new DocumentQuarantinedEvent(
                scanJob.getId(),
                scanJob.getDocumentId(),
                scanJob.getVersionId(),
                "tenant-placeholder", // Will be overridden or derived in orchestrator
                null
        ));
    }
}
