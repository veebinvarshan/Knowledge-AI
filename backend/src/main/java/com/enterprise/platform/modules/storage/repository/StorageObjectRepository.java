package com.enterprise.platform.modules.storage.repository;

import com.enterprise.platform.modules.storage.domain.StorageObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StorageObjectRepository extends JpaRepository<StorageObject, UUID> {
    Optional<StorageObject> findByLogicalPath(String logicalPath);
    boolean existsByLogicalPath(String logicalPath);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(s.sizeBytes), 0) FROM StorageObject s WHERE s.logicalPath LIKE concat(:tenantId, '/%')")
    long sumSizeBytesByTenantId(@org.springframework.data.repository.query.Param("tenantId") String tenantId);
}
