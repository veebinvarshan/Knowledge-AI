package com.enterprise.platform.modules.storage;

import com.enterprise.platform.core.config.properties.LocalStorageProperties;
import com.enterprise.platform.infrastructure.storage.LocalStorageProvider;
import com.enterprise.platform.modules.storage.service.dto.StorageLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectKeyGenerationTest {

    @TempDir
    Path tempDir;

    private LocalStorageProvider provider;

    @BeforeEach
    void setUp() {
        LocalStorageProperties properties = new LocalStorageProperties(tempDir.toString());
        provider = new LocalStorageProvider(properties);
    }

    @Test
    void testLogicalPathDoesNotMapToPhysicalFilename() throws Exception {
        // GIVEN
        String logicalPath = "my/confidential/doc.pdf";
        InputStream in = new ByteArrayInputStream("confidential-data".getBytes(StandardCharsets.UTF_8));

        // WHEN
        StorageLocation location = provider.store(in, logicalPath, "application/pdf");

        // THEN
        assertNotNull(location);
        assertNotEquals(logicalPath, location.providerObjectKey());
        // Verify key is a UUID + extension format
        assertTrue(location.providerObjectKey().endsWith(".pdf"));
        assertFalse(location.providerObjectKey().contains("/"));
        assertFalse(location.providerObjectKey().contains("my"));
    }
}
