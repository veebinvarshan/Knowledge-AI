package com.enterprise.platform.modules.virusscan.repository;

import com.enterprise.platform.modules.virusscan.domain.ScanJob;
import com.enterprise.platform.modules.virusscan.domain.ScanJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScanJobRepository extends JpaRepository<ScanJob, UUID> {
    Optional<ScanJob> findByVersionId(UUID versionId);
    List<ScanJob> findAllByStatusInAndNextRetryAtBefore(List<ScanJobStatus> statuses, Instant time);
}
