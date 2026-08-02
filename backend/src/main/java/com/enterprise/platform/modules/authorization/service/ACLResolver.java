package com.enterprise.platform.modules.authorization.service;

import com.enterprise.platform.modules.authorization.domain.ResourceAcl;
import com.enterprise.platform.modules.authorization.domain.Role;
import com.enterprise.platform.modules.authorization.repository.ResourceAclRepository;
import com.enterprise.platform.modules.authorization.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ACLResolver {

    private static final Logger log = LoggerFactory.getLogger(ACLResolver.class);

    private final ResourceAclRepository resourceAclRepository;
    private final RoleRepository roleRepository;
    private final List<ResourceHierarchyProvider> hierarchyProviders;

    public ACLResolver(
            ResourceAclRepository resourceAclRepository,
            RoleRepository roleRepository,
            List<ResourceHierarchyProvider> hierarchyProviders) {
        this.resourceAclRepository = resourceAclRepository;
        this.roleRepository = roleRepository;
        this.hierarchyProviders = hierarchyProviders;
    }

    @Transactional(readOnly = true)
    public boolean hasAccess(UUID userId, String tenantId, Set<String> roleNames, String resourceType, UUID resourceId, String requiredLevel) {
        // 1. Owner Override Check
        UUID ownerId = resolveOwnerId(resourceType, resourceId);
        if (ownerId != null && ownerId.equals(userId)) {
            log.debug("Access ALLOWED by Owner Override for resource {}", resourceId);
            return true;
        }

        // Fetch User's Role UUIDs to match against Role ACLs
        Set<UUID> userRoleIds = new HashSet<>();
        for (String roleName : roleNames) {
            roleRepository.findByName(roleName).ifPresent(r -> userRoleIds.add(r.getId()));
        }

        // Walk hierarchy from current node upwards
        String currentType = resourceType;
        UUID currentId = resourceId;

        while (currentId != null) {
            // Evaluate ACL at this specific layer
            Optional<Boolean> decision = evaluateAclAtLayer(userId, userRoleIds, tenantId, currentType, currentId, requiredLevel);
            if (decision.isPresent()) {
                log.debug("Access decision '{}' resolved at layer {}:{}", decision.get(), currentType, currentId);
                return decision.get();
            }

            // Move to parent hierarchy
            ResourceHierarchyProvider provider = findProvider(currentType);
            if (provider != null) {
                UUID parentId = provider.getParentId(currentId);
                String parentType = provider.getParentType(currentId);
                currentId = parentId;
                currentType = parentType;
            } else {
                break;
            }
        }

        // No ACL entry resolved, defer check to global roles
        log.debug("No ACL entries found in hierarchy. Deferring to role-permission checks.");
        return false;
    }

    private Optional<Boolean> evaluateAclAtLayer(UUID userId, Set<UUID> roleIds, String tenantId, String resourceType, UUID resourceId, String requiredLevel) {
        List<ResourceAcl> acls = resourceAclRepository.findByTenantIdAndResourceTypeAndResourceId(tenantId, resourceType, resourceId);
        if (acls.isEmpty()) {
            return Optional.empty();
        }

        boolean explicitDeny = false;
        boolean hasAllow = false;

        // User-specific ACL holds precedence over Role-specific ACL
        ResourceAcl userAcl = null;
        List<ResourceAcl> matchingRoleAcls = new ArrayList<>();

        for (ResourceAcl acl : acls) {
            if (acl.getIdentityId() != null && acl.getIdentityId().equals(userId)) {
                userAcl = acl;
            } else if (acl.getRoleId() != null && roleIds.contains(acl.getRoleId())) {
                matchingRoleAcls.add(acl);
            }
        }

        // 1. Evaluate User ACL first
        if (userAcl != null) {
            if ("DENY".equalsIgnoreCase(userAcl.getPermissionLevel())) {
                return Optional.of(false); // Explicit deny overrides
            }
            if (isLevelSatisfied(userAcl.getPermissionLevel(), requiredLevel)) {
                return Optional.of(true);
            }
            return Optional.of(false); // Direct user ACL exists but level is insufficient
        }

        // 2. Evaluate Role ACLs
        for (ResourceAcl acl : matchingRoleAcls) {
            if ("DENY".equalsIgnoreCase(acl.getPermissionLevel())) {
                explicitDeny = true;
            } else if (isLevelSatisfied(acl.getPermissionLevel(), requiredLevel)) {
                hasAllow = true;
            }
        }

        if (explicitDeny) {
            return Optional.of(false); // Deny overrides
        }
        if (hasAllow) {
            return Optional.of(true);
        }

        return Optional.empty();
    }

    private boolean isLevelSatisfied(String actual, String required) {
        if (actual == null || required == null) return false;
        int actualRank = getLevelRank(actual);
        int requiredRank = getLevelRank(required);
        return actualRank >= requiredRank && actualRank >= 0;
    }

    private int getLevelRank(String level) {
        return switch (level.toUpperCase()) {
            case "READ" -> 1;
            case "WRITE" -> 2;
            case "MANAGE" -> 3;
            default -> -1; // DENY or other
        };
    }

    private UUID resolveOwnerId(String type, UUID id) {
        ResourceHierarchyProvider provider = findProvider(type);
        return provider != null ? provider.getOwnerId(id) : null;
    }

    private ResourceHierarchyProvider findProvider(String resourceType) {
        for (ResourceHierarchyProvider provider : hierarchyProviders) {
            if (provider.supports(resourceType)) {
                return provider;
            }
        }
        return null;
    }
}
