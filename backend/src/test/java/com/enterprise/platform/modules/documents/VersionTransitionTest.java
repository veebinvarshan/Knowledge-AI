package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class VersionTransitionTest {

    @Test
    void testActiveToSupersededTransition() {
        // GIVEN
        Document document = new Document("doc-1", "doc-1", null, "tenant-1", UUID.randomUUID(), UUID.randomUUID());

        // WHEN (First version added)
        DocumentVersion v1 = document.addVersion(
                UUID.randomUUID(), "checksum1", "SHA256", 100L, "application/pdf", UUID.randomUUID(), VersionType.INITIAL, "Initial upload"
        );

        // THEN
        assertEquals(VersionStatus.ACTIVE, v1.getStatus());
        assertEquals(1, v1.getVersionNumber());
        assertEquals(v1.getId(), document.getCurrentVersionId());

        // WHEN (Second version added)
        DocumentVersion v2 = document.addVersion(
                UUID.randomUUID(), "checksum2", "SHA256", 200L, "application/pdf", UUID.randomUUID(), VersionType.USER_UPLOAD, "Second version"
        );

        // THEN
        assertEquals(VersionStatus.SUPERSEDED, v1.getStatus());
        assertEquals(VersionStatus.ACTIVE, v2.getStatus());
        assertEquals(2, v2.getVersionNumber());
        assertEquals(v2.getId(), document.getCurrentVersionId());
    }
}
