package com.enterprise.platform.modules.search.repository;

import com.enterprise.platform.modules.search.domain.SearchJob;
import com.enterprise.platform.modules.search.domain.SearchJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SearchJobRepository extends JpaRepository<SearchJob, UUID> {
    Optional<SearchJob> findByVersionId(UUID versionId);
    List<SearchJob> findAllByStatusInAndStartedAtBefore(List<SearchJobStatus> statuses, Instant time);
}
