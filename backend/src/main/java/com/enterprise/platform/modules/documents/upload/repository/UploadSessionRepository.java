package com.enterprise.platform.modules.documents.upload.repository;

import com.enterprise.platform.modules.documents.upload.domain.UploadSession;
import com.enterprise.platform.modules.documents.upload.domain.UploadSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface UploadSessionRepository extends JpaRepository<UploadSession, UUID> {
    List<UploadSession> findAllByStatusInAndExpiresAtBefore(List<UploadSessionStatus> statuses, Instant now);
    long countByUserIdAndStatusIn(UUID userId, List<UploadSessionStatus> statuses);
    long countByTenantIdAndStatusIn(String tenantId, List<UploadSessionStatus> statuses);
}
