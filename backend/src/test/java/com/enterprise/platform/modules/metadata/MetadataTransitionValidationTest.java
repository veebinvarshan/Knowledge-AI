package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.modules.metadata.domain.MetadataJob;
import com.enterprise.platform.modules.metadata.domain.MetadataJobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataTransitionValidationTest {

    @Test
    void testValidTransitions() {
        MetadataJob job = new MetadataJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "tenant-1");
        assertEquals(MetadataJobStatus.PENDING, job.getStatus());

        // PENDING -> EXTRACTING
        assertDoesNotThrow(job::transitionToExtracting);
        assertEquals(MetadataJobStatus.EXTRACTING, job.getStatus());

        // EXTRACTING -> COMPLETED
        assertDoesNotThrow(() -> job.transitionToCompleted(150, "TIKA"));
        assertEquals(MetadataJobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void testFailedAndRetryingTransitions() {
        MetadataJob job = new MetadataJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "tenant-1");
        job.transitionToExtracting();

        // EXTRACTING -> FAILED
        assertDoesNotThrow(() -> job.transitionToFailed("Timeout"));
        assertEquals(MetadataJobStatus.FAILED, job.getStatus());

        // FAILED -> RETRYING
        assertDoesNotThrow(job::transitionToRetrying);
        assertEquals(MetadataJobStatus.RETRYING, job.getStatus());
        assertEquals(1, job.getRetryCount());

        // RETRYING -> EXTRACTING
        assertDoesNotThrow(job::transitionToExtracting);
        assertEquals(MetadataJobStatus.EXTRACTING, job.getStatus());
    }

    @Test
    void testInvalidTransitionsRejected() {
        MetadataJob job = new MetadataJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "tenant-1");

        // PENDING -> COMPLETED is invalid (must go through EXTRACTING)
        assertThrows(IllegalStateException.class, () -> job.transitionToCompleted(100, "TIKA"));

        // PENDING -> FAILED is invalid
        assertThrows(IllegalStateException.class, () -> job.transitionToFailed("IO error"));
    }
}
