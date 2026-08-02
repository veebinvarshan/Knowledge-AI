package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchJob;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SearchLatencyMetricsTest {

    @Test
    void testExecutionTimeTracksMilliseconds() {
        SemanticSearchJob job = new SemanticSearchJob(
                "tenant-1", UUID.randomUUID(), "hash", "QDRANT", "model", "COSINE"
        );
        job.transitionToEmbedding();
        job.transitionToSearching();
        job.transitionToCompleted(5, 120L, true);

        assertEquals(120L, job.getExecutionTimeMs());
        assertTrue(job.getCacheHit());
    }
}
