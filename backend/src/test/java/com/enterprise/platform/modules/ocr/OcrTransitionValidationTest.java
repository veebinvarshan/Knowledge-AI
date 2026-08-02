package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.modules.ocr.domain.OcrJob;
import com.enterprise.platform.modules.ocr.domain.OcrJobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OcrTransitionValidationTest {

    @Test
    void testValidTransitions() {
        OcrJob job = new OcrJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "tenant-1");
        assertEquals(OcrJobStatus.PENDING, job.getStatus());

        // PENDING -> PROCESSING
        assertDoesNotThrow(job::transitionToProcessing);
        assertEquals(OcrJobStatus.PROCESSING, job.getStatus());

        // PROCESSING -> COMPLETED
        assertDoesNotThrow(() -> job.transitionToCompleted(120, 85.0, 3, "Tesseract"));
        assertEquals(OcrJobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void testSkippedTransition() {
        OcrJob job = new OcrJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "tenant-1");
        job.transitionToProcessing();

        // PROCESSING -> SKIPPED
        assertDoesNotThrow(() -> job.transitionToSkipped("Searchable PDF"));
        assertEquals(OcrJobStatus.SKIPPED, job.getStatus());
        assertEquals("Searchable PDF", job.getErrorMessage());
    }

    @Test
    void testFailedAndRetryingTransitions() {
        OcrJob job = new OcrJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "tenant-1");
        job.transitionToProcessing();

        // PROCESSING -> FAILED
        assertDoesNotThrow(() -> job.transitionToFailed("Timeout"));
        assertEquals(OcrJobStatus.FAILED, job.getStatus());

        // FAILED -> RETRYING
        assertDoesNotThrow(job::transitionToRetrying);
        assertEquals(OcrJobStatus.RETRYING, job.getStatus());
        assertEquals(1, job.getRetryCount());

        // RETRYING -> PROCESSING
        assertDoesNotThrow(job::transitionToProcessing);
        assertEquals(OcrJobStatus.PROCESSING, job.getStatus());
    }

    @Test
    void testInvalidTransitionsRejected() {
        OcrJob job = new OcrJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "tenant-1");

        // PENDING -> COMPLETED is invalid (must go through PROCESSING)
        assertThrows(IllegalStateException.class, () -> job.transitionToCompleted(100, 90.0, 1, "Tesseract"));

        // PENDING -> SKIPPED is invalid
        assertThrows(IllegalStateException.class, () -> job.transitionToSkipped("Already done"));
    }
}
