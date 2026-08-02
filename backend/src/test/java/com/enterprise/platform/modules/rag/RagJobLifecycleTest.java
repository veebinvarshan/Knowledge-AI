package com.enterprise.platform.modules.rag;

import com.enterprise.platform.modules.rag.domain.RagJob;
import com.enterprise.platform.modules.rag.domain.RagJobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RagJobLifecycleTest {

    @Test
    void testStateTransitions() {
        RagJob job = new RagJob("tenant-1", UUID.randomUUID(), "query text", 2000);
        assertEquals(RagJobStatus.RECEIVED, job.getStatus());

        job.transitionToRetrieving();
        assertEquals(RagJobStatus.RETRIEVING, job.getStatus());

        job.transitionToConstructingContext();
        assertEquals(RagJobStatus.CONSTRUCTING_CONTEXT, job.getStatus());

        job.transitionToGenerating("template");
        assertEquals(RagJobStatus.GENERATING, job.getStatus());

        job.transitionToCompleted("response", 3, 250L);
        assertEquals(RagJobStatus.COMPLETED, job.getStatus());
        assertEquals("response", job.getResponseText());
        assertEquals(3, job.getCitationCount());
        assertEquals(250L, job.getExecutionTimeMs());
    }

    @Test
    void testInvalidTransitionThrows() {
        RagJob job = new RagJob("tenant-1", UUID.randomUUID(), "query text", 2000);
        assertThrows(IllegalStateException.class, job::transitionToConstructingContext);
    }
}
