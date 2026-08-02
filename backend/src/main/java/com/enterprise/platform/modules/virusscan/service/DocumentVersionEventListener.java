package com.enterprise.platform.modules.virusscan.service;

import com.enterprise.platform.modules.documents.domain.DocumentVersionEvents.DocumentVersionCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentVersionEventListener {

    private final VirusScanService virusScanService;

    public DocumentVersionEventListener(VirusScanService virusScanService) {
        this.virusScanService = virusScanService;
    }

    @EventListener
    public void onVersionCreated(DocumentVersionCreatedEvent event) {
        virusScanService.submitScanJob(
                event.getTenantId(),
                event.getUserId(),
                event.getDocumentId(),
                event.getVersionId(),
                event.getStorageObjectId()
        );
    }
}
