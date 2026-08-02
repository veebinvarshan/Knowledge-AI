package com.enterprise.platform.modules.metadata.listener;

import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.domain.VirusScanEvents.VirusScanCompletedEvent;
import com.enterprise.platform.modules.virusscan.repository.ScanJobRepository;
import com.enterprise.platform.modules.metadata.service.MetadataExtractionService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VirusScanCompletedEventListener {

    private final MetadataExtractionService metadataService;
    private final ScanJobRepository scanJobRepository;

    public VirusScanCompletedEventListener(MetadataExtractionService metadataService, ScanJobRepository scanJobRepository) {
        this.metadataService = metadataService;
        this.scanJobRepository = scanJobRepository;
    }

    @EventListener
    public void onScanCompleted(VirusScanCompletedEvent event) {
        ScanJob scanJob = scanJobRepository.findById(event.getScanJobId()).orElse(null);
        if (scanJob != null) {
            metadataService.submitExtractionJob(
                    event.getTenantId(),
                    event.getUserId(),
                    event.getDocumentId(),
                    event.getVersionId(),
                    scanJob.getStorageObjectId()
            );
        }
    }
}
