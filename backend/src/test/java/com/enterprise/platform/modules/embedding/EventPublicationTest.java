package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.domain.EmbeddingEvents;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventPublicationTest {

    @Test
    void testEmbeddingRequestedEventHasCorrectProperties() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        UUID jobId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        EmbeddingEvents.EmbeddingGenerationRequestedEvent event = new EmbeddingEvents.EmbeddingGenerationRequestedEvent(
                jobId, docId, versionId, "tenant-1", "GEMINI", "model", "v1"
        );

        publisher.publishEvent(event);

        assertEquals(1, event.getEventVersion());
        assertEquals("tenant-1", event.getTenantId());
        verify(publisher).publishEvent(event);
    }
}
