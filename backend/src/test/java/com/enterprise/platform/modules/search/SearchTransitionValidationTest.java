package com.enterprise.platform.modules.search;

import com.enterprise.platform.modules.search.domain.SearchJob;
import com.enterprise.platform.modules.search.domain.SearchJobStatus;
import com.enterprise.platform.modules.search.domain.SearchIndexType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SearchTransitionValidationTest {

    @Test
    void testValidTransitions() {
        SearchJob job = new SearchJob(UUID.randomUUID(), UUID.randomUUID(), "tenant-1", SearchIndexType.HYBRID);
        assertEquals(SearchJobStatus.PENDING, job.getStatus());

        // PENDING -> INDEXING
        assertDoesNotThrow(job::transitionToIndexing);
        assertEquals(SearchJobStatus.INDEXING, job.getStatus());

        // INDEXING -> COMPLETED
        assertDoesNotThrow(() -> job.transitionToCompleted(150L));
        assertEquals(SearchJobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void testFailedAndRetryingTransitions() {
        SearchJob job = new SearchJob(UUID.randomUUID(), UUID.randomUUID(), "tenant-1", SearchIndexType.HYBRID);
        job.transitionToIndexing();

        // INDEXING -> FAILED
        assertDoesNotThrow(() -> job.transitionToFailed("Lock exception"));
        assertEquals(SearchJobStatus.FAILED, job.getStatus());

        // FAILED -> RETRYING
        assertDoesNotThrow(job::transitionToRetrying);
        assertEquals(SearchJobStatus.RETRYING, job.getStatus());
        assertEquals(1, job.getRetryCount());

        // RETRYING -> INDEXING
        assertDoesNotThrow(job::transitionToIndexing);
        assertEquals(SearchJobStatus.INDEXING, job.getStatus());
    }

    @Test
    void testSkippedTransition() {
        SearchJob job = new SearchJob(UUID.randomUUID(), UUID.randomUUID(), "tenant-1", SearchIndexType.HYBRID);
        job.transitionToIndexing();

        // INDEXING -> SKIPPED
        assertDoesNotThrow(() -> job.transitionToSkipped("Disabled"));
        assertEquals(SearchJobStatus.SKIPPED, job.getStatus());
        assertEquals("Disabled", job.getErrorMessage());
    }

    @Test
    void testInvalidTransitionsRejected() {
        SearchJob job = new SearchJob(UUID.randomUUID(), UUID.randomUUID(), "tenant-1", SearchIndexType.HYBRID);

        // PENDING -> COMPLETED is invalid (must go through INDEXING)
        assertThrows(IllegalStateException.class, () -> job.transitionToCompleted(100L));
    }
}
