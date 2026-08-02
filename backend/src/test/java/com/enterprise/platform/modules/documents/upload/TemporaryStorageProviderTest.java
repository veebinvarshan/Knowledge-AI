package com.enterprise.platform.modules.documents.upload;

import com.enterprise.platform.core.config.properties.LocalStorageProperties;
import com.enterprise.platform.infrastructure.upload.LocalTemporaryStorageProvider;
import com.enterprise.platform.modules.documents.upload.service.dto.TemporaryResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TemporaryStorageProviderTest {

    @TempDir
    Path tempDir;

    private LocalTemporaryStorageProvider provider;

    @BeforeEach
    void setUp() {
        LocalStorageProperties properties = new LocalStorageProperties(tempDir.toString());
        provider = new LocalTemporaryStorageProvider(properties);
    }

    @Test
    void testStoreAndRetrieveChunksSuccess() throws Exception {
        // GIVEN
        UUID sessionId = UUID.randomUUID();
        String content = "Chunk data content";
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // WHEN
        provider.storeChunk(sessionId, 1, in, content.length());

        // THEN
        TemporaryResource resource = provider.retrieveChunk(sessionId, 1);
        assertNotNull(resource);
        assertEquals(content.length(), resource.sizeBytes());
        
        String readData = new String(resource.inputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(content, readData);

        // Delete Session
        provider.deleteSessionData(sessionId);
        Path sessionDir = tempDir.resolve("temp").resolve(sessionId.toString());
        assertFalse(Files.exists(sessionDir));
    }
}
