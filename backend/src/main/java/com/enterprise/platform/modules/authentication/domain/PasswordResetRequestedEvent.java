package com.enterprise.platform.modules.authentication.domain;

import java.util.UUID;

public record PasswordResetRequestedEvent(
    UUID identityId,
    String email,
    UUID tokenValue
) implements com.enterprise.platform.core.audit.AuditableEvent {
    @Override
    public String getTenantId() { return "UNKNOWN"; }
    @Override
    public UUID getUserId() { return identityId; }
    @Override
    public String getEntityType() { return "USER"; }
    @Override
    public String getEntityId() { return identityId.toString(); }
    @Override
    public String getAction() { return "PASSWORD_RESET_REQUEST"; }
}
