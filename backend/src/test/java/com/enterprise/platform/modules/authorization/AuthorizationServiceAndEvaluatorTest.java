package com.enterprise.platform.modules.authorization;

import com.enterprise.platform.modules.authorization.domain.*;
import com.enterprise.platform.modules.authorization.repository.*;
import com.enterprise.platform.modules.authorization.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthorizationServiceAndEvaluatorTest {

    private ResourceAclRepository resourceAclRepository;
    private RoleRepository roleRepository;
    private UserRoleRepository userRoleRepository;
    private List<ResourceHierarchyProvider> hierarchyProviders;

    private RoleResolver roleResolver;
    private PermissionResolver permissionResolver;
    private ACLResolver aclResolver;
    private SecurityEvaluatorImpl securityEvaluator;

    private UUID userId;
    private String tenantId;
    private UUID docId;
    private UUID folderId;
    private UUID kbId;

    @BeforeEach
    void setUp() {
        resourceAclRepository = mock(ResourceAclRepository.class);
        roleRepository = mock(RoleRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        hierarchyProviders = new ArrayList<>();

        roleResolver = new RoleResolver(userRoleRepository);
        permissionResolver = new PermissionResolver(roleResolver);
        aclResolver = new ACLResolver(resourceAclRepository, roleRepository, hierarchyProviders);
        securityEvaluator = new SecurityEvaluatorImpl(aclResolver);

        userId = UUID.randomUUID();
        tenantId = "acme-corp";
        docId = UUID.randomUUID();
        folderId = UUID.randomUUID();
        kbId = UUID.randomUUID();
    }

    @Test
    void testRoleResolverDynamicInheritance() {
        // GIVEN a hierarchy: ROLE_ORG_ADMIN -> ROLE_MANAGER -> ROLE_EDITOR
        Role orgAdmin = new Role("ROLE_ORG_ADMIN", "Admin", tenantId);
        Role manager = new Role("ROLE_MANAGER", "Manager", tenantId);
        Role editor = new Role("ROLE_EDITOR", "Editor", tenantId);

        orgAdmin.getChildRoles().add(manager);
        manager.getChildRoles().add(editor);

        UserRole userRole = new UserRole(userId, orgAdmin, tenantId);
        when(userRoleRepository.findByIdentityIdAndTenantId(userId, tenantId))
                .thenReturn(List.of(userRole));

        // WHEN resolving roles
        Set<Role> resolved = roleResolver.resolveRoles(userId, tenantId);

        // THEN user inherits all sub-roles
        assertTrue(resolved.contains(orgAdmin));
        assertTrue(resolved.contains(manager));
        assertTrue(resolved.contains(editor));
        assertEquals(3, resolved.size());
    }

    @Test
    void testOwnerOverrideCheck() {
        // GIVEN user is the owner of the document
        ResourceHierarchyProvider provider = mock(ResourceHierarchyProvider.class);
        when(provider.supports("DOCUMENT")).thenReturn(true);
        when(provider.getOwnerId(docId)).thenReturn(userId);
        hierarchyProviders.add(provider);

        // WHEN checking ACL access
        boolean hasAccess = aclResolver.hasAccess(userId, tenantId, Set.of("ROLE_VIEWER"), "DOCUMENT", docId, "WRITE");

        // THEN access should be granted via owner override
        assertTrue(hasAccess);
    }

    @Test
    void testDirectAclUserDenyPrecedence() {
        // GIVEN a document has user direct ALLOW and user direct DENY entries
        ResourceAcl userDeny = new ResourceAcl(tenantId, "DOCUMENT", docId, userId, null, "DENY");

        when(resourceAclRepository.findByTenantIdAndResourceTypeAndResourceId(tenantId, "DOCUMENT", docId))
                .thenReturn(List.of(userDeny));

        // WHEN checking access
        boolean hasAccess = aclResolver.hasAccess(userId, tenantId, Set.of("ROLE_VIEWER"), "DOCUMENT", docId, "READ");

        // THEN access should be denied
        assertFalse(hasAccess);
    }

    @Test
    void testInheritedFolderAccessAllowed() {
        // GIVEN a document has no direct ACL, but the parent folder allows the user
        ResourceHierarchyProvider hierarchyProvider = mock(ResourceHierarchyProvider.class);
        when(hierarchyProvider.supports("DOCUMENT")).thenReturn(true);
        when(hierarchyProvider.getParentId(docId)).thenReturn(folderId);
        when(hierarchyProvider.getParentType(docId)).thenReturn("FOLDER");
        hierarchyProviders.add(hierarchyProvider);

        ResourceAcl folderAllow = new ResourceAcl(tenantId, "FOLDER", folderId, userId, null, "READ");
        when(resourceAclRepository.findByTenantIdAndResourceTypeAndResourceId(tenantId, "FOLDER", folderId))
                .thenReturn(List.of(folderAllow));
        when(resourceAclRepository.findByTenantIdAndResourceTypeAndResourceId(tenantId, "DOCUMENT", docId))
                .thenReturn(Collections.emptyList());

        // WHEN checking access
        boolean hasAccess = aclResolver.hasAccess(userId, tenantId, Set.of("ROLE_VIEWER"), "DOCUMENT", docId, "READ");

        // THEN access is allowed via inherited folder ACL
        assertTrue(hasAccess);
    }

    @Test
    void testTenantBoundaryIsolationViolation() {
        // GIVEN a document from tenant 'other-corp' is queried by user from tenant 'acme-corp'
        ResourceAcl otherCorpAcl = new ResourceAcl("other-corp", "DOCUMENT", docId, userId, null, "READ");
        when(resourceAclRepository.findByTenantIdAndResourceTypeAndResourceId("acme-corp", "DOCUMENT", docId))
                .thenReturn(Collections.emptyList());

        // WHEN checking access inside 'acme-corp' context
        boolean hasAccess = aclResolver.hasAccess(userId, "acme-corp", Set.of("ROLE_VIEWER"), "DOCUMENT", docId, "READ");

        // THEN access must be denied to maintain tenant boundary isolation
        assertFalse(hasAccess);
    }

    @Test
    void testUserWithAllPermissions() {
        // GIVEN security context is populated with all permissions
        AuthorizationContext mockCtx = new AuthorizationContext(
                userId, tenantId, null,
                Set.of("ROLE_SUPER_ADMIN"),
                Set.of("system:manage", "documents:read", "documents:create"),
                "session-123", "device-123"
        );

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(mockCtx);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // WHEN evaluating permissions
        boolean hasSystemPerm = securityEvaluator.hasPermission("system:manage");
        boolean hasDocumentRead = securityEvaluator.hasPermission("documents:read");

        // THEN permissions are resolved successfully
        assertTrue(hasSystemPerm);
        assertTrue(hasDocumentRead);
    }

    @Test
    void testUserWithoutRequiredPermission() {
        // GIVEN security context is populated with limited permissions
        AuthorizationContext mockCtx = new AuthorizationContext(
                userId, tenantId, null,
                Set.of("ROLE_VIEWER"),
                Set.of("documents:read"),
                "session-123", "device-123"
        );

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(mockCtx);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // WHEN evaluating a missing permission
        boolean hasWrite = securityEvaluator.hasPermission("documents:write");

        // THEN access should be denied
        assertFalse(hasWrite);
    }
}
