package com.enterprise.platform.modules.authorization.repository;

import com.enterprise.platform.modules.authorization.domain.ResourceAcl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceAclRepository extends JpaRepository<ResourceAcl, UUID> {
    List<ResourceAcl> findByResourceTypeAndResourceId(String resourceType, UUID resourceId);
    List<ResourceAcl> findByTenantIdAndResourceTypeAndResourceId(String tenantId, String resourceType, UUID resourceId);
    List<ResourceAcl> findByIdentityId(UUID identityId);
}
