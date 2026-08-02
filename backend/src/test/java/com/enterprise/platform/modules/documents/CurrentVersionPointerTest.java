package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CurrentVersionPointerTest {

    @Test
    void testCurrentVersionPointerUpdates() {
        // GIVEN
        Document document = new Document("doc-1", "doc-1", null, "tenant-1", UUID.randomUUID(), UUID.randomUUID());
        assertNull(document.getCurrentVersionId());

        // WHEN
        DocumentVersion v1 = document.addVersion(
                UUID.randomUUID(), "checksum1", "SHA256", 100L, "text/plain", UUID.randomUUID(), VersionType.INITIAL, ""
        );

        // THEN
        assertEquals(v1.getId(), document.getCurrentVersionId());

        // WHEN
        DocumentVersion v2 = document.addVersion(
                UUID.randomUUID(), "checksum2", "SHA256", 100L, "text/plain", UUID.randomUUID(), VersionType.USER_UPLOAD, ""
        );

        // THEN
        assertEquals(v2.getId(), document.getCurrentVersionId());
    }
}
