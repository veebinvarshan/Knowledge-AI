package com.enterprise.platform.modules.ocr.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ocr_jobs", indexes = {
    @Index(name = "idx_ocr_jobs_version", columnList = "version_id"),
    @Index(name = "idx_ocr_jobs_status", columnList = "status")
})
public class OcrJob {

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

    @Column(name = "provider")
    private String provider;

    @Column(name = "language")
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OcrJobStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "page_count")
    private Integer pageCount;

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

    public OcrJob() {}

    public OcrJob(UUID documentId, UUID versionId, UUID storageObjectId, String tenantId) {
        this.documentId = documentId;
        this.versionId = versionId;
        this.storageObjectId = storageObjectId;
        this.tenantId = tenantId;
        this.status = OcrJobStatus.PENDING;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public UUID getDocumentId() { return documentId; }
    public UUID getVersionId() { return versionId; }
    public UUID getStorageObjectId() { return storageObjectId; }
    public String getTenantId() { return tenantId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public OcrJobStatus getStatus() { return status; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Aggregate state transitions mapping with validations
    public void transitionToProcessing() {
        validateTransition(OcrJobStatus.PROCESSING);
        this.status = OcrJobStatus.PROCESSING;
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void transitionToCompleted(long durationMs, double avgConfidence, int pages, String provider) {
        validateTransition(OcrJobStatus.COMPLETED);
        this.status = OcrJobStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.durationMs = durationMs;
        this.confidenceScore = avgConfidence;
        this.pageCount = pages;
        this.provider = provider;
    }

    public void transitionToFailed(String errorMessage) {
        validateTransition(OcrJobStatus.FAILED);
        this.status = OcrJobStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public void transitionToRetrying() {
        validateTransition(OcrJobStatus.RETRYING);
        this.status = OcrJobStatus.RETRYING;
        this.retryCount++;
    }

    public void transitionToSkipped(String reason) {
        validateTransition(OcrJobStatus.SKIPPED);
        this.status = OcrJobStatus.SKIPPED;
        this.completedAt = Instant.now();
        this.errorMessage = reason; // Treat errorMessage as skipped reason
    }

    private void validateTransition(OcrJobStatus target) {
        boolean valid = false;
        switch (this.status) {
            case PENDING -> valid = (target == OcrJobStatus.PROCESSING);
            case PROCESSING -> valid = (target == OcrJobStatus.COMPLETED || target == OcrJobStatus.FAILED || target == OcrJobStatus.SKIPPED);
            case FAILED -> valid = (target == OcrJobStatus.RETRYING);
            case RETRYING -> valid = (target == OcrJobStatus.PROCESSING);
            default -> valid = false;
        }
        if (!valid) {
            throw new IllegalStateException("Invalid OCR job transition from " + this.status + " to " + target);
        }
    }
}
