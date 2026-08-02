package com.enterprise.platform.modules.authorization.bootstrap;

import com.enterprise.platform.modules.authorization.domain.Permission;
import com.enterprise.platform.modules.authorization.domain.Role;
import com.enterprise.platform.modules.authorization.repository.PermissionRepository;
import com.enterprise.platform.modules.authorization.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class RolePermissionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RolePermissionSeeder.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public RolePermissionSeeder(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting role and permission registration checks...");
        seedPermissionsAndRoles();
        log.info("Role and permission registration checks completed.");
    }

    private void seedPermissionsAndRoles() {
        // 1. Define all permissions
        Map<String, String> defaultPermissions = new LinkedHashMap<>();
        defaultPermissions.put("documents:read", "Read document files and view text content");
        defaultPermissions.put("documents:create", "Create new document metadata nodes");
        defaultPermissions.put("documents:update", "Modify document metadata or contents");
        defaultPermissions.put("documents:delete", "Delete or archive documents");
        defaultPermissions.put("documents:share", "Share documents and configure ACL lists");
        
        defaultPermissions.put("folders:read", "Navigate and view folder lists");
        defaultPermissions.put("folders:create", "Create new folders in the repository");
        defaultPermissions.put("folders:update", "Rename or move folders");
        defaultPermissions.put("folders:delete", "Delete folders and nested contents");
        defaultPermissions.put("folders:archive", "Archive folders and nested contents");
        defaultPermissions.put("folders:restore", "Restore archived or deleted folders");
        
        defaultPermissions.put("knowledge:read", "View knowledge base configurations");
        defaultPermissions.put("knowledge:create", "Provision new knowledge base domains");
        defaultPermissions.put("knowledge:update", "Modify knowledge base properties");
        defaultPermissions.put("knowledge:delete", "Tear down knowledge bases");
        defaultPermissions.put("knowledge:manage", "Manage knowledge base sync intervals and index mappings");
        
        defaultPermissions.put("chat:ask", "Send vector search queries to chat AI engine");
        defaultPermissions.put("chat:history", "Retrieve past conversations log list");
        defaultPermissions.put("chat:manage", "Clear or delete chat conversations");
        
        defaultPermissions.put("users:read", "View tenant user identities lists");
        defaultPermissions.put("users:manage", "Assign roles and lock/unlock user accounts");
        
        defaultPermissions.put("roles:read", "View dynamic roles definition lists");
        defaultPermissions.put("roles:manage", "Create custom roles and modify permission keys");
        
        defaultPermissions.put("analytics:view", "Inspect system utilization audit metrics");
        defaultPermissions.put("settings:manage", "Modify tenant workspace preferences");
        defaultPermissions.put("system:manage", "Configure global server parameters (Super Admin only)");

        Map<String, Permission> seededPermissions = new HashMap<>();

        for (Map.Entry<String, String> entry : defaultPermissions.entrySet()) {
            Optional<Permission> existing = permissionRepository.findByAction(entry.getKey());
            if (existing.isEmpty()) {
                Permission permission = new Permission(entry.getKey(), entry.getValue());
                permissionRepository.save(permission);
                seededPermissions.put(entry.getKey(), permission);
                log.info("Seeded permission: {}", entry.getKey());
            } else {
                seededPermissions.put(entry.getKey(), existing.get());
            }
        }

        // 2. Define default roles and their permission lists
        seedRole("ROLE_SUPER_ADMIN", "Super Administrator with full access to global parameters", seededPermissions.keySet(), seededPermissions);
        
        seedRole("ROLE_ORG_ADMIN", "Organization Administrator with full access to tenant boundaries", 
            Set.of(
                "documents:read", "documents:create", "documents:update", "documents:delete", "documents:share",
                "folders:read", "folders:create", "folders:update", "folders:delete", "folders:archive", "folders:restore",
                "knowledge:read", "knowledge:create", "knowledge:update", "knowledge:delete", "knowledge:manage",
                "chat:ask", "chat:history", "chat:manage",
                "users:read", "users:manage",
                "roles:read", "roles:manage",
                "analytics:view", "settings:manage"
            ), seededPermissions);

        seedRole("ROLE_MANAGER", "Workspace Manager who controls content allocations and audits", 
            Set.of(
                "documents:read", "documents:create", "documents:update", "documents:share",
                "folders:read", "folders:create", "folders:update", "folders:delete", "folders:archive", "folders:restore",
                "knowledge:read", "knowledge:manage",
                "chat:ask", "chat:history", "chat:manage",
                "users:read", "roles:read"
            ), seededPermissions);

        seedRole("ROLE_EDITOR", "Content Editor who can modify files and collections", 
            Set.of(
                "documents:read", "documents:create", "documents:update", "documents:share",
                "folders:read", "folders:create", "folders:update",
                "knowledge:read",
                "chat:ask", "chat:history"
            ), seededPermissions);

        seedRole("ROLE_CONTRIBUTOR", "Content Contributor who can submit and edit own documents", 
            Set.of(
                "documents:read", "documents:create",
                "folders:read",
                "knowledge:read",
                "chat:ask", "chat:history"
            ), seededPermissions);

        seedRole("ROLE_VIEWER", "Read-only access to authorized knowledge bases", 
            Set.of("documents:read", "folders:read", "knowledge:read", "chat:ask", "chat:history"), seededPermissions);

        seedRole("ROLE_GUEST", "Sandboxed guest access for specifically shared documents", 
            Set.of("documents:read", "chat:ask"), seededPermissions);
    }

    private void seedRole(String name, String description, Set<String> permissionKeys, Map<String, Permission> seededPermissions) {
        Optional<Role> existingRole = roleRepository.findByName(name);
        if (existingRole.isEmpty()) {
            Role role = new Role(name, description, null); // null tenantId for global defaults
            Set<Permission> rolePerms = new HashSet<>();
            for (String key : permissionKeys) {
                Permission p = seededPermissions.get(key);
                if (p != null) {
                    rolePerms.add(p);
                }
            }
            role.setPermissions(rolePerms);
            roleRepository.save(role);
            log.info("Seeded role: {}", name);
        }
    }
}
