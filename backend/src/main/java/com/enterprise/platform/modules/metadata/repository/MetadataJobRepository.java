package com.enterprise.platform.modules.metadata.repository;

import com.enterprise.platform.modules.metadata.domain.MetadataJob;
import com.enterprise.platform.modules.metadata.domain.MetadataJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetadataJobRepository extends JpaRepository<MetadataJob, UUID> {
    Optional<MetadataJob> findByVersionId(UUID versionId);
    List<MetadataJob> findAllByStatusInAndStartedAtBefore(List<MetadataJobStatus> statuses, Instant time);
}
