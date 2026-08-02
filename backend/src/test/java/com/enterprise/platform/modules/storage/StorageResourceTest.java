package com.enterprise.platform.modules.storage;

import com.enterprise.platform.modules.storage.service.dto.StorageMetadata;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class StorageResourceTest {

    @Test
    void testStorageResourceAttributes() throws Exception {
        // GIVEN
        String content = "file-contents";
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        StorageMetadata meta = new StorageMetadata(13, "application/pdf", "checksum123", "SHA256");

        // WHEN
        StorageResource resource = new StorageResource(in, meta);

        // THEN
        assertNotNull(resource.inputStream());
        assertEquals(meta, resource.metadata());
        assertEquals(13, resource.metadata().sizeBytes());
        assertEquals("application/pdf", resource.metadata().mimeType());
        assertEquals("checksum123", resource.metadata().checksum());
        assertEquals("SHA256", resource.metadata().checksumAlgorithm());
    }
}
