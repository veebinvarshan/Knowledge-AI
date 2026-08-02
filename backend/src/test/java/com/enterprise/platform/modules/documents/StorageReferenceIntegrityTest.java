package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StorageReferenceIntegrityTest {

    @Test
    void testStorageObjectIdsAreMappedCorrectly() {
        // GIVEN
        Document document = new Document("doc-1", "doc-1", null, "tenant-1", UUID.randomUUID(), UUID.randomUUID());
        UUID storageObjectId = UUID.randomUUID();

        // WHEN
        DocumentVersion v = document.addVersion(
                storageObjectId, "checksum-abc", "SHA256", 1024L, "image/png", UUID.randomUUID(), VersionType.INITIAL, "mapped"
        );

        // THEN
        assertEquals(storageObjectId, v.getStorageObjectId());
        assertEquals("checksum-abc", v.getChecksum());
        assertEquals("SHA256", v.getChecksumAlgorithm());
        assertEquals(1024L, v.getSizeBytes());
        assertEquals("image/png", v.getMimeType());
    }
}
