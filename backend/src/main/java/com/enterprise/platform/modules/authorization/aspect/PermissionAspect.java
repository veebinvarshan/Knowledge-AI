package com.enterprise.platform.modules.authorization.aspect;

import com.enterprise.platform.modules.authorization.annotation.RequirePermission;
import com.enterprise.platform.modules.authorization.service.SecurityEvaluator;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    private final SecurityEvaluator securityEvaluator;

    public PermissionAspect(SecurityEvaluator securityEvaluator) {
        this.securityEvaluator = securityEvaluator;
    }

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        String permission = requirePermission.value();
        if (!securityEvaluator.hasPermission(permission)) {
            throw new AccessDeniedException("Access Denied: Lacking permission '" + permission + "'");
        }
    }
}
