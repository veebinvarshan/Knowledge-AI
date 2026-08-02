package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchEvents;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SearchAnalyticsEventTest {

    @Test
    void testSemanticSearchRequestedEventValues() {
        UUID jobId = UUID.randomUUID();
        SemanticSearchEvents.SemanticSearchRequestedEvent event = new SemanticSearchEvents.SemanticSearchRequestedEvent(
                jobId, "tenant-1", "QDRANT", "model", "COSINE"
        );

        assertEquals(jobId, event.getJobId());
        assertEquals("tenant-1", event.getTenantId());
        assertEquals("model", event.getEmbeddingModel());
        assertEquals("COSINE", event.getSimilarityMetric());
    }
}
