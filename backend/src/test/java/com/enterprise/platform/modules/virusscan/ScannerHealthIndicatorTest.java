package com.enterprise.platform.modules.virusscan;

import com.enterprise.platform.core.config.properties.ClamAvProperties;
import com.enterprise.platform.core.config.properties.VirusScanProperties;
import com.enterprise.platform.infrastructure.virusscan.VirusScannerHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class ScannerHealthIndicatorTest {

    @Test
    void testHealthReturnsUpWhenDisabled() {
        // GIVEN
        VirusScanProperties virusScanProperties = new VirusScanProperties(false, "CLAMAV", 3, 1000, 2, 10, "QUARANTINE");
        ClamAvProperties clamAvProperties = new ClamAvProperties("localhost", 3310, 1000, 1000);
        ObjectProvider<ClamAvProperties> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable(any())).thenReturn(clamAvProperties);
        VirusScannerHealthIndicator healthIndicator = new VirusScannerHealthIndicator(provider, virusScanProperties);

        // WHEN
        Health health = healthIndicator.health();

        // THEN
        assertEquals(Status.UP, health.getStatus());
        assertEquals("Disabled by configuration", health.getDetails().get("status"));
    }

    @Test
    void testHealthReturnsDownOnConnectionFailure() {
        // GIVEN (Points to invalid host/port to trigger connection failure)
        VirusScanProperties virusScanProperties = new VirusScanProperties(true, "CLAMAV", 3, 1000, 2, 10, "QUARANTINE");
        ClamAvProperties clamAvProperties = new ClamAvProperties("invalid-host-xyz", 9999, 50, 50);
        ObjectProvider<ClamAvProperties> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable(any())).thenReturn(clamAvProperties);
        VirusScannerHealthIndicator healthIndicator = new VirusScannerHealthIndicator(provider, virusScanProperties);

        // WHEN
        Health health = healthIndicator.health();

        // THEN
        assertEquals(Status.DOWN, health.getStatus());
        assertNotNull(health.getDetails().get("error"));
    }
}
