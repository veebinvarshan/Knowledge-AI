package com.enterprise.platform.modules.metadata.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "metadata_jobs", indexes = {
    @Index(name = "idx_metadata_jobs_version", columnList = "version_id"),
    @Index(name = "idx_metadata_jobs_status", columnList = "status")
})
public class MetadataJob {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID jobId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "storage_object_id", nullable = false)
    private UUID storageObjectId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "extractor_provider")
    private String extractorProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MetadataJobStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public MetadataJob() {}

    public MetadataJob(UUID documentId, UUID versionId, UUID storageObjectId, String tenantId) {
        this.documentId = documentId;
        this.versionId = versionId;
        this.storageObjectId = storageObjectId;
        this.tenantId = tenantId;
        this.status = MetadataJobStatus.PENDING;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public UUID getDocumentId() { return documentId; }
    public UUID getVersionId() { return versionId; }
    public UUID getStorageObjectId() { return storageObjectId; }
    public String getTenantId() { return tenantId; }
    
    public String getExtractorProvider() { return extractorProvider; }
    public void setExtractorProvider(String extractorProvider) { this.extractorProvider = extractorProvider; }

    public MetadataJobStatus getStatus() { return status; }
    public Integer getRetryCount() { return retryCount; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Aggregate state transitions mapping with validations
    public void transitionToExtracting() {
        validateTransition(MetadataJobStatus.EXTRACTING);
        this.status = MetadataJobStatus.EXTRACTING;
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void transitionToCompleted(long durationMs, String provider) {
        validateTransition(MetadataJobStatus.COMPLETED);
        this.status = MetadataJobStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.durationMs = durationMs;
        this.extractorProvider = provider;
    }

    public void transitionToFailed(String errorMessage) {
        validateTransition(MetadataJobStatus.FAILED);
        this.status = MetadataJobStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public void transitionToRetrying() {
        validateTransition(MetadataJobStatus.RETRYING);
        this.status = MetadataJobStatus.RETRYING;
        this.retryCount++;
    }

    private void validateTransition(MetadataJobStatus target) {
        boolean valid = false;
        switch (this.status) {
            case PENDING -> valid = (target == MetadataJobStatus.EXTRACTING);
            case EXTRACTING -> valid = (target == MetadataJobStatus.COMPLETED || target == MetadataJobStatus.FAILED);
            case FAILED -> valid = (target == MetadataJobStatus.RETRYING);
            case RETRYING -> valid = (target == MetadataJobStatus.EXTRACTING);
            default -> valid = false;
        }
        if (!valid) {
            throw new IllegalStateException("Invalid metadata job transition from " + this.status + " to " + target);
        }
    }
}
