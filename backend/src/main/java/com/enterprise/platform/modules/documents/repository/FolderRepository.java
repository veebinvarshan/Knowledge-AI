package com.enterprise.platform.modules.documents.repository;

import com.enterprise.platform.modules.documents.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findByTenantIdAndParentFolderIdAndDeletedAtIsNull(String tenantId, UUID parentFolderId);

    @Query("SELECT f FROM Folder f WHERE f.tenantId = :tenantId AND f.parentFolderId IS NULL AND f.deletedAt IS NULL")
    List<Folder> findRootFolders(@Param("tenantId") String tenantId);

    Optional<Folder> findByTenantIdAndParentFolderIdAndNameAndDeletedAtIsNull(String tenantId, UUID parentFolderId, String name);

    @Query("SELECT f FROM Folder f WHERE f.tenantId = :tenantId AND f.parentFolderId IS NULL AND f.name = :name AND f.deletedAt IS NULL")
    Optional<Folder> findRootFolderByName(@Param("tenantId") String tenantId, @Param("name") String name);

    List<Folder> findByMaterializedPathStartingWithAndTenantId(String prefix, String tenantId);

    boolean existsByTenantIdAndParentFolderIdAndNameAndDeletedAtIsNullAndIdNot(String tenantId, UUID parentFolderId, String name, UUID id);

    @Query("SELECT COUNT(f) > 0 FROM Folder f WHERE f.tenantId = :tenantId AND f.parentFolderId IS NULL AND f.name = :name AND f.deletedAt IS NULL AND f.id <> :id")
    boolean existsRootFolderByNameAndIdNot(@Param("tenantId") String tenantId, @Param("name") String name, @Param("id") UUID id);
}
