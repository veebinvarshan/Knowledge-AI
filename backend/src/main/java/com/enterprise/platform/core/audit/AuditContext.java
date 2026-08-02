package com.enterprise.platform.core.audit;

import java.util.UUID;

public record AuditContext(
    UUID actorId,
    String tenantId,
    String requestId
) {}
