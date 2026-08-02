package com.enterprise.platform.modules.authorization.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_user_roles")
@IdClass(UserRoleId.class)
public class UserRole {

    @Id
    @Column(name = "identity_id", nullable = false)
    private UUID identityId;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    public UserRole() {}

    public UserRole(UUID identityId, Role role, String tenantId) {
        this.identityId = identityId;
        this.role = role;
        this.tenantId = tenantId;
    }

    // Getters and Setters
    public UUID getIdentityId() { return identityId; }
    public void setIdentityId(UUID identityId) { this.identityId = identityId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
