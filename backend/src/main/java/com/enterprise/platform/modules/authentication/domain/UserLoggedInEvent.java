package com.enterprise.platform.modules.authentication.domain;

import java.util.UUID;

public record UserLoggedInEvent(
    UUID identityId,
    String email,
    String tenantId,
    String ipAddress
) implements com.enterprise.platform.core.audit.AuditableEvent {
    @Override
    public String getTenantId() { return tenantId; }
    @Override
    public UUID getUserId() { return identityId; }
    @Override
    public String getEntityType() { return "USER"; }
    @Override
    public String getEntityId() { return identityId.toString(); }
    @Override
    public String getAction() { return "LOGIN"; }
}
