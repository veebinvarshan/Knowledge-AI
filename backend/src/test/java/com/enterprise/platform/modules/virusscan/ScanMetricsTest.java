package com.enterprise.platform.modules.virusscan;

import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.domain.ScanJobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ScanMetricsTest {

    @Test
    void testMetricsFieldsPopulatedOnTransition() {
        // GIVEN
        ScanJob job = new ScanJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        job.transitionToScanning();

        // WHEN
        job.transitionToClean("ClamAV-Engine", "1.4.2", 120L, 4096L);

        // THEN
        assertEquals(ScanJobStatus.CLEAN, job.getStatus());
        assertEquals("ClamAV-Engine", job.getEngineName());
        assertEquals("1.4.2", job.getEngineVersion());
        assertEquals(120L, job.getScanDurationMs());
        assertEquals(4096L, job.getBytesScanned());
        assertNull(job.getSignatureName());
    }

    @Test
    void testInfectedMetricsFieldsPopulated() {
        // GIVEN
        ScanJob job = new ScanJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        job.transitionToScanning();

        // WHEN
        job.transitionToInfected("ClamAV-Engine", "1.4.2", "Eicar-Signature-Test", 85L, 2048L);

        // THEN
        assertEquals(ScanJobStatus.INFECTED, job.getStatus());
        assertEquals("ClamAV-Engine", job.getEngineName());
        assertEquals("1.4.2", job.getEngineVersion());
        assertEquals("Eicar-Signature-Test", job.getSignatureName());
        assertEquals(85L, job.getScanDurationMs());
        assertEquals(2048L, job.getBytesScanned());
    }
}
