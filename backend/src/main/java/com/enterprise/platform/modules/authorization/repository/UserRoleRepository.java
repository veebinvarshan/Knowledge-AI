package com.enterprise.platform.modules.authorization.repository;

import com.enterprise.platform.modules.authorization.domain.UserRole;
import com.enterprise.platform.modules.authorization.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByIdentityId(UUID identityId);
    List<UserRole> findByIdentityIdAndTenantId(UUID identityId, String tenantId);
}
