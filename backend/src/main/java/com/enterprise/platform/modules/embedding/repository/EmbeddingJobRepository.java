package com.enterprise.platform.modules.embedding.repository;

import com.enterprise.platform.modules.embedding.domain.EmbeddingJob;
import com.enterprise.platform.modules.embedding.domain.EmbeddingJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmbeddingJobRepository extends JpaRepository<EmbeddingJob, UUID> {
    List<EmbeddingJob> findAllByVersionId(UUID versionId);
    List<EmbeddingJob> findAllByStatus(EmbeddingJobStatus status);
}
