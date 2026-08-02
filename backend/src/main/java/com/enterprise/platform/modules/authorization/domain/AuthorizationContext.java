package com.enterprise.platform.modules.authorization.domain;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class AuthorizationContext implements Principal {

    private final UUID userId;
    private final String tenantId;
    private final UUID workspaceId;
    private final Set<String> activeRoles;
    private final Set<String> effectivePermissions;
    private final String sessionId;
    private final String deviceFingerprint;

    public AuthorizationContext(
            UUID userId,
            String tenantId,
            UUID workspaceId,
            Set<String> activeRoles,
            Set<String> effectivePermissions,
            String sessionId,
            String deviceFingerprint) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.activeRoles = activeRoles != null ? Collections.unmodifiableSet(activeRoles) : Collections.emptySet();
        this.effectivePermissions = effectivePermissions != null ? Collections.unmodifiableSet(effectivePermissions) : Collections.emptySet();
        this.sessionId = sessionId;
        this.deviceFingerprint = deviceFingerprint;
    }

    @Override
    public String getName() {
        return userId.toString();
    }

    public UUID getUserId() { return userId; }
    public String getTenantId() { return tenantId; }
    public UUID getWorkspaceId() { return workspaceId; }
    public Set<String> getActiveRoles() { return activeRoles; }
    public Set<String> getEffectivePermissions() { return effectivePermissions; }
    public String getSessionId() { return sessionId; }
    public String getDeviceFingerprint() { return deviceFingerprint; }

    public boolean hasPermission(String permission) {
        return effectivePermissions.contains(permission);
    }

    @Override
    public String toString() {
        return "AuthorizationContext{" +
               "userId=" + userId +
               ", tenantId='" + tenantId + '\'' +
               ", activeRoles=" + activeRoles +
               '}';
    }
}
