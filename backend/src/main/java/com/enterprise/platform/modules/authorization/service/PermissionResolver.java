package com.enterprise.platform.modules.authorization.service;

import com.enterprise.platform.modules.authorization.domain.Permission;
import com.enterprise.platform.modules.authorization.domain.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PermissionResolver {

    private final RoleResolver roleResolver;

    public PermissionResolver(RoleResolver roleResolver) {
        this.roleResolver = roleResolver;
    }

    /**
     * Resolves the list of all effective permission strings for a user identity.
     */
    @Transactional(readOnly = true)
    public Set<String> resolvePermissions(UUID identityId, String tenantId) {
        Set<Role> roles = roleResolver.resolveRoles(identityId, tenantId);
        Set<String> permissionActions = new HashSet<>();

        for (Role role : roles) {
            if (role.getPermissions() != null) {
                for (Permission p : role.getPermissions()) {
                    if ("ACTIVE".equalsIgnoreCase(p.getStatus())) {
                        permissionActions.add(p.getAction());
                    }
                }
            }
        }

        return permissionActions;
    }
}
