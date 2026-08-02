package com.enterprise.platform.modules.semanticsearch.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "semantic_search_analytics")
public class SemanticSearchJob {

    @Id
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "query_hash", nullable = false)
    private String queryHash;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "embedding_model", nullable = false)
    private String embeddingModel;

    @Column(name = "similarity_metric", nullable = false)
    private String similarityMetric;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs = 0L;

    @Column(name = "result_count")
    private Integer resultCount = 0;

    @Column(name = "cache_hit")
    private Boolean cacheHit = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SemanticSearchJobStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SemanticSearchJob() {}

    public SemanticSearchJob(String tenantId, UUID userId, String queryHash, String provider, String embeddingModel, String similarityMetric) {
        this.jobId = UUID.randomUUID();
        this.tenantId = tenantId;
        this.userId = userId;
        this.queryHash = queryHash;
        this.provider = provider;
        this.embeddingModel = embeddingModel;
        this.similarityMetric = similarityMetric;
        this.status = SemanticSearchJobStatus.RECEIVED;
        this.createdAt = Instant.now();
    }

    public void transitionToEmbedding() {
        if (this.status != SemanticSearchJobStatus.RECEIVED) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to EMBEDDING");
        }
        this.status = SemanticSearchJobStatus.EMBEDDING;
    }

    public void transitionToSearching() {
        if (this.status != SemanticSearchJobStatus.EMBEDDING && this.status != SemanticSearchJobStatus.RETRYING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to SEARCHING");
        }
        this.status = SemanticSearchJobStatus.SEARCHING;
    }

    public void transitionToCompleted(int resultCount, long executionTimeMs, boolean cacheHit) {
        if (this.status != SemanticSearchJobStatus.SEARCHING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to COMPLETED");
        }
        this.status = SemanticSearchJobStatus.COMPLETED;
        this.resultCount = resultCount;
        this.executionTimeMs = executionTimeMs;
        this.cacheHit = cacheHit;
    }

    public void transitionToFailed() {
        if (this.status != SemanticSearchJobStatus.SEARCHING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to FAILED");
        }
        this.status = SemanticSearchJobStatus.FAILED;
    }

    public void transitionToRetrying() {
        if (this.status != SemanticSearchJobStatus.FAILED) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to RETRYING");
        }
        this.status = SemanticSearchJobStatus.RETRYING;
    }

    // Getters
    public UUID getJobId() { return jobId; }
    public String getTenantId() { return tenantId; }
    public UUID getUserId() { return userId; }
    public String getQueryHash() { return queryHash; }
    public String getProvider() { return provider; }
    public String getEmbeddingModel() { return embeddingModel; }
    public String getSimilarityMetric() { return similarityMetric; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public Integer getResultCount() { return resultCount; }
    public Boolean getCacheHit() { return cacheHit; }
    public SemanticSearchJobStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
