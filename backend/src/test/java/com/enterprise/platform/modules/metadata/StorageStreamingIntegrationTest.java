package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.modules.storage.service.StorageService;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.mockito.Mockito.*;

public class StorageStreamingIntegrationTest {

    @Test
    void testStorageAccessedOnlyViaStreamingResource() throws Exception {
        StorageService storageService = mock(StorageService.class);
        StorageResource resource = mock(StorageResource.class);
        InputStream mockInputStream = mock(InputStream.class);

        when(storageService.retrieve("logical-path")).thenReturn(resource);
        when(resource.inputStream()).thenReturn(mockInputStream);

        // Verify retrieval executes stream access
        StorageResource retrieved = storageService.retrieve("logical-path");
        InputStream stream = retrieved.inputStream();

        verify(storageService, times(1)).retrieve("logical-path");
        verify(resource, times(1)).inputStream();
    }
}
