package com.enterprise.platform.modules.authorization.service;

import com.enterprise.platform.modules.authorization.domain.Role;
import com.enterprise.platform.modules.authorization.domain.UserRole;
import com.enterprise.platform.modules.authorization.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoleResolver {

    private final UserRoleRepository userRoleRepository;

    public RoleResolver(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Resolves all active roles for the identity, including dynamically inherited child roles.
     */
    @Transactional(readOnly = true)
    public Set<Role> resolveRoles(UUID identityId, String tenantId) {
        List<UserRole> userRoles = userRoleRepository.findByIdentityIdAndTenantId(identityId, tenantId);
        
        Set<Role> resolvedRoles = new HashSet<>();
        Queue<Role> queue = new LinkedList<>();

        for (UserRole ur : userRoles) {
            Role r = ur.getRole();
            if (r != null) {
                queue.add(r);
            }
        }

        // BFS to resolve all nested/child roles in the dynamic hierarchy
        while (!queue.isEmpty()) {
            Role current = queue.poll();
            if (resolvedRoles.add(current)) {
                // Fetch child roles if present to traverse hierarchy
                if (current.getChildRoles() != null) {
                    for (Role child : current.getChildRoles()) {
                        if (!resolvedRoles.contains(child)) {
                            queue.add(child);
                        }
                    }
                }
            }
        }

        return resolvedRoles;
    }
}
