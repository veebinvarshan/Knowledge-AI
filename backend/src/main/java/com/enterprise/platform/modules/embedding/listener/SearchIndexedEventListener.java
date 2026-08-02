package com.enterprise.platform.modules.embedding.listener;

import com.enterprise.platform.modules.embedding.service.EmbeddingService;
import com.enterprise.platform.modules.search.domain.SearchEvents.SearchIndexedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SearchIndexedEventListener {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexedEventListener.class);

    private final EmbeddingService embeddingService;

    public SearchIndexedEventListener(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @EventListener
    @Async
    public void onSearchIndexed(SearchIndexedEvent event) {
        log.info("Received SearchIndexedEvent for version: {}. Submitting embedding job.", event.getVersionId());
        try {
            embeddingService.submitEmbeddingJob(
                    event.getDocumentId(),
                    event.getVersionId(),
                    event.getTenantId()
            );
        } catch (Exception e) {
            log.error("Failed to automatically submit embedding job for version {}", event.getVersionId(), e);
        }
    }
}
