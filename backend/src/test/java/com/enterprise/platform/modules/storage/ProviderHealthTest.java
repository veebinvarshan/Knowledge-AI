package com.enterprise.platform.modules.storage;

import com.enterprise.platform.modules.storage.service.StorageProvider;
import com.enterprise.platform.modules.storage.service.dto.StorageHealth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProviderHealthTest {

    @Test
    void testProviderHealthAttributes() {
        // GIVEN
        StorageProvider provider = mock(StorageProvider.class);
        StorageHealth health = new StorageHealth("LOCAL", "UP", "Local storage is active and writable");
        when(provider.checkHealth()).thenReturn(health);

        // WHEN
        StorageHealth result = provider.checkHealth();

        // THEN
        assertNotNull(result);
        assertEquals("LOCAL", result.providerId());
        assertEquals("UP", result.status());
        assertEquals("Local storage is active and writable", result.message());
    }
}
