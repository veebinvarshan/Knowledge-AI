package com.enterprise.platform.modules.authorization.service;

import com.enterprise.platform.modules.authorization.domain.AuthorizationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("sec")
public class SecurityEvaluatorImpl implements SecurityEvaluator {

    private static final Logger log = LoggerFactory.getLogger(SecurityEvaluatorImpl.class);

    private final ACLResolver aclResolver;

    public SecurityEvaluatorImpl(ACLResolver aclResolver) {
        this.aclResolver = aclResolver;
    }

    @Override
    public boolean hasPermission(String permission) {
        AuthorizationContext context = getContext();
        if (context == null) return false;

        return context.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(String permission, UUID resourceId, String resourceType) {
        AuthorizationContext context = getContext();
        if (context == null) return false;

        // 1. Resolve permission action to ACL level rank
        String requiredLevel = getRequiredAclLevel(permission);

        // 2. Query ACL Resolver to traverse ownership & folder hierarchies
        boolean hasAclAccess = aclResolver.hasAccess(
                context.getUserId(),
                context.getTenantId(),
                context.getActiveRoles(),
                resourceType,
                resourceId,
                requiredLevel
        );

        if (hasAclAccess) {
            return true;
        }

        // 3. Fallback: If no explicit ACL allowed/denied, check standard tenant role permissions
        return context.hasPermission(permission);
    }

    private String getRequiredAclLevel(String permission) {
        if (permission == null) return "READ";
        
        String action = permission.contains(":") 
                ? permission.substring(permission.indexOf(":") + 1).toLowerCase() 
                : permission.toLowerCase();

        return switch (action) {
            case "read", "view" -> "READ";
            case "write", "create", "update", "edit" -> "WRITE";
            case "delete", "remove", "share", "manage" -> "MANAGE";
            default -> "READ";
        };
    }

    private AuthorizationContext getContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthorizationContext) {
            return (AuthorizationContext) principal;
        }
        return null;
    }
}
