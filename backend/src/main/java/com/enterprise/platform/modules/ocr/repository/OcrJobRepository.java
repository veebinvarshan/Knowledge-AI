package com.enterprise.platform.modules.ocr.repository;

import com.enterprise.platform.modules.ocr.domain.OcrJob;
import com.enterprise.platform.modules.ocr.domain.OcrJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OcrJobRepository extends JpaRepository<OcrJob, UUID> {
    Optional<OcrJob> findByVersionId(UUID versionId);
    List<OcrJob> findAllByStatusInAndStartedAtBefore(List<OcrJobStatus> statuses, Instant time);
}
