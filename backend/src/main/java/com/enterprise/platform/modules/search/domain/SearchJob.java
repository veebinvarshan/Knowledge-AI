package com.enterprise.platform.modules.search.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_jobs", indexes = {
    @Index(name = "idx_search_jobs_version", columnList = "version_id"),
    @Index(name = "idx_search_jobs_status", columnList = "status")
})
public class SearchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID jobId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SearchJobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "index_type", nullable = false, length = 50)
    private SearchIndexType indexType;

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

    public SearchJob() {}

    public SearchJob(UUID documentId, UUID versionId, String tenantId, SearchIndexType indexType) {
        this.documentId = documentId;
        this.versionId = versionId;
        this.tenantId = tenantId;
        this.indexType = indexType;
        this.status = SearchJobStatus.PENDING;
    }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public UUID getDocumentId() { return documentId; }
    public UUID getVersionId() { return versionId; }
    public String getTenantId() { return tenantId; }

    public SearchJobStatus getStatus() { return status; }
    public SearchIndexType getIndexType() { return indexType; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Aggregate state transitions mapping with validations
    public void transitionToIndexing() {
        validateTransition(SearchJobStatus.INDEXING);
        this.status = SearchJobStatus.INDEXING;
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void transitionToCompleted(long durationMs) {
        validateTransition(SearchJobStatus.COMPLETED);
        this.status = SearchJobStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.durationMs = durationMs;
    }

    public void transitionToFailed(String errorMessage) {
        validateTransition(SearchJobStatus.FAILED);
        this.status = SearchJobStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public void transitionToRetrying() {
        validateTransition(SearchJobStatus.RETRYING);
        this.status = SearchJobStatus.RETRYING;
        this.retryCount++;
    }

    public void transitionToSkipped(String reason) {
        validateTransition(SearchJobStatus.SKIPPED);
        this.status = SearchJobStatus.SKIPPED;
        this.completedAt = Instant.now();
        this.errorMessage = reason;
    }

    private void validateTransition(SearchJobStatus target) {
        boolean valid = false;
        switch (this.status) {
            case PENDING -> valid = (target == SearchJobStatus.INDEXING);
            case INDEXING -> valid = (target == SearchJobStatus.COMPLETED || target == SearchJobStatus.FAILED || target == SearchJobStatus.SKIPPED);
            case FAILED -> valid = (target == SearchJobStatus.RETRYING);
            case RETRYING -> valid = (target == SearchJobStatus.INDEXING);
            default -> valid = false;
        }
        if (!valid) {
            throw new IllegalStateException("Invalid Search job transition from " + this.status + " to " + target);
        }
    }
}
