package com.enterprise.platform.modules.storage;

import com.enterprise.platform.core.config.properties.StorageProperties;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import com.enterprise.platform.modules.storage.service.StorageProviderResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProviderResolverTest {

    @Test
    void testResolveActiveProviderCorrectly() {
        // GIVEN
        StorageProvider local = mock(StorageProvider.class);
        when(local.getProviderId()).thenReturn("LOCAL");

        StorageProvider s3 = mock(StorageProvider.class);
        when(s3.getProviderId()).thenReturn("S3");

        StorageProperties properties = new StorageProperties("S3", 1000L);

        StorageProviderResolver resolver = new StorageProviderResolver(List.of(local, s3), properties);

        // WHEN
        StorageProvider active = resolver.resolveActiveProvider();

        // THEN
        assertNotNull(active);
        assertEquals("S3", active.getProviderId());
    }

    @Test
    void testResolveNonExistentProviderThrows() {
        // GIVEN (Points to AZURE but it is not registered in the resolver constructor)
        StorageProperties properties = new StorageProperties("AZURE", 1000L);
        StorageProviderResolver resolver = new StorageProviderResolver(List.of(), properties);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, resolver::resolveActiveProvider);
    }
}
