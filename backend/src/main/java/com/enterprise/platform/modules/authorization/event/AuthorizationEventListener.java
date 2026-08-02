package com.enterprise.platform.modules.authorization.event;

import com.enterprise.platform.modules.authorization.event.AuthorizationEvents.*;
import com.enterprise.platform.modules.authorization.service.PermissionCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuthorizationEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationEventListener.class);

    private final PermissionCacheService cacheService;

    public AuthorizationEventListener(PermissionCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoleAssignment(RoleAssignmentEvent event) {
        log.info("Role assignment changed for user {}. Evicting permission cache.", event.getIdentityId());
        cacheService.evictUserPermissions(event.getIdentityId(), event.getTenantId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAclModification(AclModifiedEvent event) {
        log.info("ACL modified for resource {}. Evicting cache for subject.", event.getResourceId());
        if (event.getIdentityId() != null) {
            cacheService.evictUserPermissions(event.getIdentityId(), event.getTenantId());
        } else {
            // Evict all for role update safety or flush cache to maintain sanity
            cacheService.evictAllPermissions();
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRolePermissionsUpdated(RolePermissionsUpdatedEvent event) {
        log.info("Role permission schema modified inside tenant {}. Flushing permissions cache.", event.getTenantId());
        cacheService.evictAllPermissions();
    }
}
