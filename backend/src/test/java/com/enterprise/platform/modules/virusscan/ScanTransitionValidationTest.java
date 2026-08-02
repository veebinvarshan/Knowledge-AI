package com.enterprise.platform.modules.virusscan;

import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.domain.ScanJobStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ScanTransitionValidationTest {

    @Test
    void testValidScanTransitions() {
        // GIVEN
        ScanJob job = new ScanJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        assertEquals(ScanJobStatus.PENDING, job.getStatus());

        // PENDING -> SCANNING
        assertDoesNotThrow(job::transitionToScanning);
        assertEquals(ScanJobStatus.SCANNING, job.getStatus());

        // SCANNING -> CLEAN
        assertDoesNotThrow(() -> job.transitionToClean("ClamAV", "1.0", 50, 100));
        assertEquals(ScanJobStatus.CLEAN, job.getStatus());
    }

    @Test
    void testInfectedAndQuarantinedTransitions() {
        // GIVEN
        ScanJob job = new ScanJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        job.transitionToScanning();

        // SCANNING -> INFECTED
        assertDoesNotThrow(() -> job.transitionToInfected("ClamAV", "1.0", "Eicar", 40, 100));
        assertEquals(ScanJobStatus.INFECTED, job.getStatus());

        // INFECTED -> QUARANTINED
        assertDoesNotThrow(job::transitionToQuarantined);
        assertEquals(ScanJobStatus.QUARANTINED, job.getStatus());
    }

    @Test
    void testInvalidScanTransitionsRejected() {
        // GIVEN
        ScanJob job = new ScanJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        // PENDING -> CLEAN is invalid (must go through SCANNING)
        assertThrows(IllegalStateException.class, () -> job.transitionToClean("ClamAV", "1.0", 50, 100));
        
        // PENDING -> INFECTED is invalid
        assertThrows(IllegalStateException.class, () -> job.transitionToInfected("ClamAV", "1.0", "Eicar", 40, 100));
    }
}
