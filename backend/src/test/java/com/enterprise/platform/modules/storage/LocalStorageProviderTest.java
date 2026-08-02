package com.enterprise.platform.modules.storage;

import com.enterprise.platform.core.config.properties.LocalStorageProperties;
import com.enterprise.platform.infrastructure.storage.LocalStorageProvider;
import com.enterprise.platform.modules.storage.exception.StorageNotFoundException;
import com.enterprise.platform.modules.storage.service.dto.StorageLocation;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LocalStorageProviderTest {

    @TempDir
    Path tempDir;

    private LocalStorageProvider provider;

    @BeforeEach
    void setUp() {
        LocalStorageProperties properties = new LocalStorageProperties(tempDir.toString());
        provider = new LocalStorageProvider(properties);
    }

    @Test
    void testStoreAndRetrieveSuccess() throws Exception {
        // GIVEN
        String content = "Hello World! Storage hardeners are active.";
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        String logicalPath = "folder/file.txt";

        // WHEN
        StorageLocation location = provider.store(in, logicalPath, "text/plain");

        // THEN
        assertNotNull(location);
        assertEquals("LOCAL", location.providerId());
        assertEquals(logicalPath, location.logicalPath());
        assertTrue(location.providerObjectKey().endsWith(".txt"));

        // Verify physical file exists
        Path physicalPath = tempDir.resolve(location.providerObjectKey());
        assertTrue(Files.exists(physicalPath));

        // RETRIEVE
        StorageResource resource = provider.retrieve(location.providerObjectKey());
        assertNotNull(resource);
        assertEquals("text/plain", resource.metadata().mimeType());
        
        String readContent = new String(resource.inputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(content, readContent);
    }

    @Test
    void testPathTraversalRequestBlocked() {
        // GIVEN
        String logicalPath = "../secrets.txt";
        InputStream in = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
                provider.store(in, logicalPath, "text/plain")
        );
    }

    @Test
    void testRetrieveNonExistentFails() {
        // GIVEN / WHEN / THEN
        assertThrows(StorageNotFoundException.class, () ->
                provider.retrieve("non-existent-key.txt")
        );
    }
}
