package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.domain.EmbeddingJob;
import com.enterprise.platform.modules.embedding.domain.EmbeddingJobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EmbeddingJobLifecycleTest {

    @Test
    void testValidTransitions() {
        EmbeddingJob job = new EmbeddingJob(UUID.randomUUID(), UUID.randomUUID(), "tenant-1", "GEMINI", "model", "v1");
        assertEquals(EmbeddingJobStatus.PENDING, job.getStatus());

        job.transitionToEmbedding();
        assertEquals(EmbeddingJobStatus.EMBEDDING, job.getStatus());

        job.transitionToCompleted(5);
        assertEquals(EmbeddingJobStatus.COMPLETED, job.getStatus());
        assertEquals(5, job.getChunkCount());
    }

    @Test
    void testInvalidTransitionThrowsException() {
        EmbeddingJob job = new EmbeddingJob(UUID.randomUUID(), UUID.randomUUID(), "tenant-1", "GEMINI", "model", "v1");
        assertThrows(IllegalStateException.class, () -> job.transitionToCompleted(5));
    }
}
