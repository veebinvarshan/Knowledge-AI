package com.enterprise.platform.modules.authorization.service;

import com.enterprise.platform.modules.authorization.domain.Role;
import java.util.Set;
import java.util.UUID;

public interface AuthorizationService {
    void assignRoleToUser(UUID identityId, String roleName, String tenantId);
    void revokeRoleFromUser(UUID identityId, String roleName, String tenantId);
    Role createCustomRole(String name, String description, Set<String> permissionActions, String tenantId);
    void grantResourceAccess(String tenantId, String resourceType, UUID resourceId, UUID identityId, UUID roleId, String permissionLevel);
    void revokeResourceAccess(String tenantId, String resourceType, UUID resourceId, UUID identityId, UUID roleId);
}
