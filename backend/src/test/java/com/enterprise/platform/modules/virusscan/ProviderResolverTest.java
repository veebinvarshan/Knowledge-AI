package com.enterprise.platform.modules.virusscan;

import com.enterprise.platform.core.config.properties.VirusScanProperties;
import com.enterprise.platform.modules.virusscan.provider.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProviderResolverTest {

    @Test
    void testResolverReturnsConfiguredProvider() {
        // GIVEN
        VirusScanProperties properties = new VirusScanProperties(true, "CLAMAV", 3, 1000, 2, 10, "QUARANTINE");
        
        VirusScannerProvider mockProvider = mock(VirusScannerProvider.class);
        VirusScanner mockScanner = mock(VirusScanner.class);
        
        when(mockProvider.getName()).thenReturn("CLAMAV");
        when(mockProvider.getScanner()).thenReturn(mockScanner);

        VirusScannerProviderResolver resolver = new VirusScannerProviderResolver(List.of(mockProvider), properties);

        // WHEN
        VirusScanner resolved = resolver.resolve();

        // THEN
        assertNotNull(resolved);
        assertEquals(mockScanner, resolved);
    }
}
