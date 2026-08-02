package com.enterprise.platform.modules.embedding.repository;

import com.enterprise.platform.modules.embedding.domain.EmbeddingChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmbeddingChunkRepository extends JpaRepository<EmbeddingChunk, UUID> {
    List<EmbeddingChunk> findAllByVersionId(UUID versionId);
    void deleteAllByVersionId(UUID versionId);
}
