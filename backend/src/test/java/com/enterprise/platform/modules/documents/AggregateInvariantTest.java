package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AggregateInvariantTest {

    @Test
    void testStateTransitionsRestrictedByAggregate() {
        // GIVEN
        Document document = new Document("doc-1", "doc-1", null, "tenant-1", UUID.randomUUID(), UUID.randomUUID());
        DocumentVersion v = document.addVersion(
                UUID.randomUUID(), "checksum1", "SHA256", 100L, "text/plain", UUID.randomUUID(), VersionType.INITIAL, "comment"
        );

        // Transition from ACTIVE to SUPERSEDED is allowed
        assertDoesNotThrow(() -> v.setStatus(VersionStatus.SUPERSEDED));
        assertEquals(VersionStatus.SUPERSEDED, v.getStatus());

        // GIVEN version set to ARCHIVED
        v.setStatus(VersionStatus.ARCHIVED);

        // WHEN trying to set to SUPERSEDED, expect IllegalStateException
        assertThrows(IllegalStateException.class, () -> v.setStatus(VersionStatus.SUPERSEDED));
    }
}
