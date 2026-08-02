package com.enterprise.platform.modules.embedding.domain;

public enum EmbeddingJobStatus {
    PENDING,
    EMBEDDING,
    COMPLETED,
    FAILED,
    RETRYING,
    SKIPPED
}
