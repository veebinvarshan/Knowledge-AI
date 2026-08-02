package com.enterprise.platform.modules.authorization.service;

import com.enterprise.platform.modules.authorization.domain.*;
import com.enterprise.platform.modules.authorization.event.AuthorizationEvents.*;
import com.enterprise.platform.modules.authorization.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ResourceAclRepository resourceAclRepository;
    private final UserRoleRepository userRoleRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AuthorizationServiceImpl(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            ResourceAclRepository resourceAclRepository,
            UserRoleRepository userRoleRepository,
            ApplicationEventPublisher eventPublisher) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.resourceAclRepository = resourceAclRepository;
        this.userRoleRepository = userRoleRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void assignRoleToUser(UUID identityId, String roleName, String tenantId) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        // Enforce tenant boundary unless super admin or role is global
        if (role.getTenantId() != null && !role.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Role tenant mismatch.");
        }

        UserRole userRole = new UserRole(identityId, role, tenantId);
        userRoleRepository.save(userRole);

        // Fire cache eviction and audit events
        eventPublisher.publishEvent(new RoleAssignmentEvent(identityId, tenantId));
    }

    @Override
    @Transactional
    public void revokeRoleFromUser(UUID identityId, String roleName, String tenantId) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        UserRoleId userRoleId = new UserRoleId(identityId, role.getId());
        userRoleRepository.findById(userRoleId).ifPresent(userRole -> {
            userRoleRepository.delete(userRole);
            eventPublisher.publishEvent(new RoleAssignmentEvent(identityId, tenantId));
        });
    }

    @Override
    @Transactional
    public Role createCustomRole(String name, String description, Set<String> permissionActions, String tenantId) {
        if (roleRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Role already exists: " + name);
        }

        Role role = new Role(name, description, tenantId);
        Set<Permission> permissions = new HashSet<>();

        for (String action : permissionActions) {
            Permission perm = permissionRepository.findByAction(action)
                    .orElseThrow(() -> new IllegalArgumentException("Permission not registered: " + action));
            permissions.add(perm);
        }

        role.setPermissions(permissions);
        Role saved = roleRepository.save(role);
        
        eventPublisher.publishEvent(new RolePermissionsUpdatedEvent(tenantId));
        return saved;
    }

    @Override
    @Transactional
    public void grantResourceAccess(String tenantId, String resourceType, UUID resourceId, UUID identityId, UUID roleId, String permissionLevel) {
        // Enforce level validity
        if (!List.of("READ", "WRITE", "MANAGE", "DENY").contains(permissionLevel.toUpperCase())) {
            throw new IllegalArgumentException("Invalid permission level: " + permissionLevel);
        }

        // Check if matching ACL exists to prevent duplicate entries
        List<ResourceAcl> existing = resourceAclRepository.findByTenantIdAndResourceTypeAndResourceId(tenantId, resourceType, resourceId);
        ResourceAcl match = null;

        for (ResourceAcl acl : existing) {
            if (identityId != null && identityId.equals(acl.getIdentityId())) {
                match = acl;
                break;
            } else if (roleId != null && roleId.equals(acl.getRoleId())) {
                match = acl;
                break;
            }
        }

        if (match != null) {
            match.setPermissionLevel(permissionLevel.toUpperCase());
            resourceAclRepository.save(match);
        } else {
            ResourceAcl newAcl = new ResourceAcl(tenantId, resourceType, resourceId, identityId, roleId, permissionLevel.toUpperCase());
            resourceAclRepository.save(newAcl);
        }

        eventPublisher.publishEvent(new AclModifiedEvent(tenantId, resourceType, resourceId, identityId, roleId));
    }

    @Override
    @Transactional
    public void revokeResourceAccess(String tenantId, String resourceType, UUID resourceId, UUID identityId, UUID roleId) {
        List<ResourceAcl> existing = resourceAclRepository.findByTenantIdAndResourceTypeAndResourceId(tenantId, resourceType, resourceId);
        
        for (ResourceAcl acl : existing) {
            if (identityId != null && identityId.equals(acl.getIdentityId())) {
                resourceAclRepository.delete(acl);
                eventPublisher.publishEvent(new AclModifiedEvent(tenantId, resourceType, resourceId, identityId, null));
                break;
            } else if (roleId != null && roleId.equals(acl.getRoleId())) {
                resourceAclRepository.delete(acl);
                eventPublisher.publishEvent(new AclModifiedEvent(tenantId, resourceType, resourceId, null, roleId));
                break;
            }
        }
    }
}
