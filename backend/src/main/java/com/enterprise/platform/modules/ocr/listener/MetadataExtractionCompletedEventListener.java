package com.enterprise.platform.modules.ocr.listener;

import com.enterprise.platform.modules.metadata.domain.MetadataEvents.MetadataExtractionCompletedEvent;
import com.enterprise.platform.modules.metadata.domain.MetadataJob;
import com.enterprise.platform.modules.metadata.repository.MetadataJobRepository;
import com.enterprise.platform.modules.ocr.service.OcrService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MetadataExtractionCompletedEventListener {

    private final OcrService ocrService;
    private final MetadataJobRepository metadataJobRepository;

    public MetadataExtractionCompletedEventListener(OcrService ocrService, MetadataJobRepository metadataJobRepository) {
        this.ocrService = ocrService;
        this.metadataJobRepository = metadataJobRepository;
    }

    @EventListener
    public void onMetadataCompleted(MetadataExtractionCompletedEvent event) {
        MetadataJob metadataJob = metadataJobRepository.findById(event.getMetadataJobId()).orElse(null);
        if (metadataJob != null) {
            ocrService.submitOcrJob(
                    event.getTenantId(),
                    null, // System trigger
                    event.getDocumentId(),
                    event.getVersionId(),
                    metadataJob.getStorageObjectId()
            );
        }
    }
}
