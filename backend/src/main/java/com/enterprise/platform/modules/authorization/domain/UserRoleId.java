package com.enterprise.platform.modules.authorization.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class UserRoleId implements Serializable {

    private UUID identityId;
    private UUID role; // maps to role.id

    public UserRoleId() {}

    public UserRoleId(UUID identityId, UUID role) {
        this.identityId = identityId;
        this.role = role;
    }

    // Getters and Setters
    public UUID getIdentityId() { return identityId; }
    public void setIdentityId(UUID identityId) { this.identityId = identityId; }

    public UUID getRole() { return role; }
    public void setRole(UUID role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleId that)) return false;
        return Objects.equals(identityId, that.identityId) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identityId, role);
    }
}
