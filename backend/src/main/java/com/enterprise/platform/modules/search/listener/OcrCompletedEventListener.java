package com.enterprise.platform.modules.search.listener;

import com.enterprise.platform.modules.ocr.domain.OcrEvents.*;
import com.enterprise.platform.modules.search.service.SearchIndexService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OcrCompletedEventListener {

    private final SearchIndexService searchIndexService;

    public OcrCompletedEventListener(SearchIndexService searchIndexService) {
        this.searchIndexService = searchIndexService;
    }

    @EventListener
    public void onOcrCompleted(OcrCompletedEvent event) {
        searchIndexService.submitIndexJob(
                event.getTenantId(),
                event.getDocumentId(),
                event.getVersionId(),
                "HYBRID"
        );
    }

    @EventListener
    public void onOcrSkipped(OcrSkippedEvent event) {
        searchIndexService.submitIndexJob(
                event.getTenantId(),
                event.getDocumentId(),
                event.getVersionId(),
                "HYBRID"
        );
    }

    @EventListener
    public void onOcrFailed(OcrFailedEvent event) {
        // Fallback: index document metadata only if OCR failed
        searchIndexService.submitIndexJob(
                event.getTenantId(),
                event.getDocumentId(),
                event.getVersionId(),
                "HYBRID"
        );
    }
}
