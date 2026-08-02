package com.enterprise.platform.modules.authorization.event;

import java.util.UUID;

public final class AuthorizationEvents {

    private AuthorizationEvents() {}

    public static class AclModifiedEvent {
        private final String tenantId;
        private final String resourceType;
        private final UUID resourceId;
        private final UUID identityId;
        private final UUID roleId;

        public AclModifiedEvent(String tenantId, String resourceType, UUID resourceId, UUID identityId, UUID roleId) {
            this.tenantId = tenantId;
            this.resourceType = resourceType;
            this.resourceId = resourceId;
            this.identityId = identityId;
            this.roleId = roleId;
        }

        public String getTenantId() { return tenantId; }
        public String getResourceType() { return resourceType; }
        public UUID getResourceId() { return resourceId; }
        public UUID getIdentityId() { return identityId; }
        public UUID getRoleId() { return roleId; }
    }

    public static class RoleAssignmentEvent {
        private final UUID identityId;
        private final String tenantId;

        public RoleAssignmentEvent(UUID identityId, String tenantId) {
            this.identityId = identityId;
            this.tenantId = tenantId;
        }

        public UUID getIdentityId() { return identityId; }
        public String getTenantId() { return tenantId; }
    }

    public static class RolePermissionsUpdatedEvent {
        private final String tenantId;

        public RolePermissionsUpdatedEvent(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getTenantId() { return tenantId; }
    }
}
