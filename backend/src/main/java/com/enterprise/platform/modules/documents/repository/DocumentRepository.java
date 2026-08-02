package com.enterprise.platform.modules.documents.repository;

import com.enterprise.platform.modules.documents.domain.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Page<Document> findByTenantIdAndFolderIdAndDeletedAtIsNull(String tenantId, UUID folderId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId AND d.folderId IS NULL AND d.deletedAt IS NULL")
    Page<Document> findRootDocuments(@Param("tenantId") String tenantId, Pageable pageable);

    Optional<Document> findByTenantIdAndSlugAndDeletedAtIsNull(String tenantId, String slug);

    List<Document> findByTenantIdAndOwnerIdAndDeletedAtIsNull(String tenantId, UUID ownerId);

    boolean existsByTenantIdAndSlugAndDeletedAtIsNullAndIdNot(String tenantId, String slug, UUID id);

    @Query(value = "SELECT COUNT(*) > 0 FROM documents d WHERE d.tenant_id = :tenantId AND d.slug = :slug AND d.deleted_at IS NULL", nativeQuery = true)
    boolean existsByTenantIdAndSlugAndDeletedAtIsNull(@Param("tenantId") String tenantId, @Param("slug") String slug);

    @Query(value = "SELECT * FROM documents d WHERE d.tenant_id = :tenantId AND d.structured_metadata ->> :key = :value AND d.deleted_at IS NULL", nativeQuery = true)
    Page<Document> findByMetadataKeyValue(
            @Param("tenantId") String tenantId,
            @Param("key") String key,
            @Param("value") String value,
            Pageable pageable
    );
}
