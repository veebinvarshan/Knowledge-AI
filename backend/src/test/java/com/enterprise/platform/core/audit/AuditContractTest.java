package com.enterprise.platform.core.audit;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuditContractTest {

    @Test
    void testAuditEventInitialization() {
        // GIVEN
        UUID actorId = UUID.randomUUID();
        String tenantId = "tenant-1";
        String requestId = "req-id";
        String action = "CREATE";
        String entity = "DOCUMENT";
        String entityId = "doc-123";

        // WHEN
        AuditEvent event = new AuditEvent(actorId, tenantId, requestId, action, entity, entityId);

        // THEN
        assertNotNull(event.timestamp());
        assertEquals(actorId, event.actorId());
        assertEquals(tenantId, event.tenantId());
        assertEquals(requestId, event.requestId());
        assertEquals(action, event.action());
        assertEquals(entity, event.entity());
        assertEquals(entityId, event.entityId());
        assertEquals(1, event.version()); // Event version starts at 1
    }
}
