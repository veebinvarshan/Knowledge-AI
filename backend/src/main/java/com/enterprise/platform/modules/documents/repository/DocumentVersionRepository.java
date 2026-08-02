package com.enterprise.platform.modules.documents.repository;

import com.enterprise.platform.modules.documents.domain.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends Repository<DocumentVersion, UUID> {
    Optional<DocumentVersion> findById(UUID id);
    Optional<DocumentVersion> findByDocumentIdAndVersionNumber(UUID documentId, int versionNumber);
    Page<DocumentVersion> findAllByDocumentId(UUID documentId, Pageable pageable);

    @Query("SELECT v FROM DocumentVersion v WHERE v.document.tenantId = :tenantId AND v.checksum = :checksum AND v.document.deletedAt IS NULL")
    List<DocumentVersion> findActiveVersionsByChecksum(@Param("tenantId") String tenantId, @Param("checksum") String checksum);
}
