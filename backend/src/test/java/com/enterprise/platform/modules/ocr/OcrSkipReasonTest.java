package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.modules.ocr.domain.OcrJob;
import com.enterprise.platform.modules.ocr.domain.OcrJobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OcrSkipReasonTest {

    @Test
    void testSkipReasonPersistedInJobErrorMessage() {
        OcrJob job = new OcrJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "tenant-1");
        job.transitionToProcessing();
        
        job.transitionToSkipped("OCR Disabled");
        
        assertEquals(OcrJobStatus.SKIPPED, job.getStatus());
        assertEquals("OCR Disabled", job.getErrorMessage());
    }
}
