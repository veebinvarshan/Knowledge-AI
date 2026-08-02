package com.enterprise.platform.modules.embedding.service;

import com.enterprise.platform.modules.embedding.domain.EmbeddingJob;
import java.util.UUID;

public interface EmbeddingService {
    EmbeddingJob submitEmbeddingJob(UUID documentId, UUID versionId, String tenantId);
    void executeEmbedding(UUID jobId);
}
