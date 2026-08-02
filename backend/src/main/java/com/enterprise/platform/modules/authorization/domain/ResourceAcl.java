package com.enterprise.platform.modules.authorization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resource_acls", indexes = {
    @Index(name = "idx_resource_acls_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_resource_acls_identity", columnList = "identity_id"),
    @Index(name = "idx_resource_acls_tenant", columnList = "tenant_id")
})
public class ResourceAcl {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "identity_id")
    private UUID identityId;

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "permission_level", nullable = false, length = 50)
    private String permissionLevel; // 'READ', 'WRITE', 'MANAGE'

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public ResourceAcl() {}

    public ResourceAcl(String tenantId, String resourceType, UUID resourceId, UUID identityId, UUID roleId, String permissionLevel) {
        this.tenantId = tenantId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.identityId = identityId;
        this.roleId = roleId;
        this.permissionLevel = permissionLevel;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public UUID getResourceId() { return resourceId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }

    public UUID getIdentityId() { return identityId; }
    public void setIdentityId(UUID identityId) { this.identityId = identityId; }

    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }

    public String getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
