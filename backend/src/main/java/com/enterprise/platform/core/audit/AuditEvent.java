package com.enterprise.platform.core.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditEvent(
    UUID actorId,
    String tenantId,
    String requestId,
    Instant timestamp,
    String action,
    String entity,
    String entityId,
    int version
) {
    public AuditEvent(UUID actorId, String tenantId, String requestId, String action, String entity, String entityId) {
        this(actorId, tenantId, requestId, Instant.now(), action, entity, entityId, 1);
    }
}
