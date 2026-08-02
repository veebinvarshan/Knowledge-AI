package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchJob;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchJobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticSearchLifecycleTest {

    @Test
    void testTransitions() {
        SemanticSearchJob job = new SemanticSearchJob(
                "tenant-1", UUID.randomUUID(), "hash", "QDRANT", "model", "COSINE"
        );
        assertEquals(SemanticSearchJobStatus.RECEIVED, job.getStatus());

        job.transitionToEmbedding();
        assertEquals(SemanticSearchJobStatus.EMBEDDING, job.getStatus());

        job.transitionToSearching();
        assertEquals(SemanticSearchJobStatus.SEARCHING, job.getStatus());

        job.transitionToCompleted(5, 20L, false);
        assertEquals(SemanticSearchJobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void testInvalidTransitions() {
        SemanticSearchJob job = new SemanticSearchJob(
                "tenant-1", UUID.randomUUID(), "hash", "QDRANT", "model", "COSINE"
        );
        assertThrows(IllegalStateException.class, job::transitionToSearching);
    }
}
