package com.enterprise.platform.modules.embedding.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "embedding_jobs")
public class EmbeddingJob {

    @Id
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "chunk_count")
    private Integer chunkCount = 0;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmbeddingJobStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    protected EmbeddingJob() {}

    public EmbeddingJob(UUID documentId, UUID versionId, String tenantId, String provider, String modelName, String modelVersion) {
        this.jobId = UUID.randomUUID();
        this.documentId = documentId;
        this.versionId = versionId;
        this.tenantId = tenantId;
        this.provider = provider;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.status = EmbeddingJobStatus.PENDING;
        this.startedAt = Instant.now();
    }

    public void transitionToEmbedding() {
        if (this.status != EmbeddingJobStatus.PENDING && this.status != EmbeddingJobStatus.RETRYING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to EMBEDDING");
        }
        this.status = EmbeddingJobStatus.EMBEDDING;
    }

    public void transitionToCompleted(int chunkCount) {
        if (this.status != EmbeddingJobStatus.EMBEDDING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to COMPLETED");
        }
        this.status = EmbeddingJobStatus.COMPLETED;
        this.chunkCount = chunkCount;
        this.completedAt = Instant.now();
        if (this.startedAt != null) {
            this.durationMs = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
        }
    }

    public void transitionToFailed(String errorMessage) {
        if (this.status != EmbeddingJobStatus.EMBEDDING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to FAILED");
        }
        this.status = EmbeddingJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
        if (this.startedAt != null) {
            this.durationMs = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
        }
    }

    public void transitionToRetrying() {
        if (this.status != EmbeddingJobStatus.FAILED) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to RETRYING");
        }
        this.status = EmbeddingJobStatus.RETRYING;
        this.retryCount++;
    }

    public void transitionToSkipped(String reason) {
        if (this.status != EmbeddingJobStatus.EMBEDDING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to SKIPPED");
        }
        this.status = EmbeddingJobStatus.SKIPPED;
        this.errorMessage = reason;
        this.completedAt = Instant.now();
    }

    // Getters only for immutability
    public UUID getJobId() { return jobId; }
    public UUID getDocumentId() { return documentId; }
    public UUID getVersionId() { return versionId; }
    public String getTenantId() { return tenantId; }
    public String getModelName() { return modelName; }
    public String getModelVersion() { return modelVersion; }
    public String getProvider() { return provider; }
    public Integer getChunkCount() { return chunkCount; }
    public Integer getRetryCount() { return retryCount; }
    public EmbeddingJobStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Long getDurationMs() { return durationMs; }
    public String getErrorMessage() { return errorMessage; }
}
