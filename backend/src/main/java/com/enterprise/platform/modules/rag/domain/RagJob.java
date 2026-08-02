package com.enterprise.platform.modules.rag.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rag_job_analytics")
public class RagJob {

    @Id
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "query_text", nullable = false)
    private String query;

    @Column(name = "prompt_template")
    private String promptTemplate;

    @Column(name = "token_budget", nullable = false)
    private int tokenBudget;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RagJobStatus status;

    @Lob
    @Column(name = "response_text")
    private String responseText;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs = 0L;

    @Column(name = "citation_count")
    private Integer citationCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RagJob() {}

    public RagJob(String tenantId, UUID userId, String query, int tokenBudget) {
        this.jobId = UUID.randomUUID();
        this.tenantId = tenantId;
        this.userId = userId;
        this.query = query;
        this.tokenBudget = tokenBudget;
        this.status = RagJobStatus.RECEIVED;
        this.createdAt = Instant.now();
    }

    public void transitionToRetrieving() {
        if (this.status != RagJobStatus.RECEIVED) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to RETRIEVING");
        }
        this.status = RagJobStatus.RETRIEVING;
    }

    public void transitionToConstructingContext() {
        if (this.status != RagJobStatus.RETRIEVING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to CONSTRUCTING_CONTEXT");
        }
        this.status = RagJobStatus.CONSTRUCTING_CONTEXT;
    }

    public void transitionToGenerating(String promptTemplate) {
        if (this.status != RagJobStatus.CONSTRUCTING_CONTEXT) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to GENERATING");
        }
        this.status = RagJobStatus.GENERATING;
        this.promptTemplate = promptTemplate;
    }

    public void transitionToCompleted(String responseText, int citationCount, long executionTimeMs) {
        if (this.status != RagJobStatus.GENERATING) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to COMPLETED");
        }
        this.status = RagJobStatus.COMPLETED;
        this.responseText = responseText;
        this.citationCount = citationCount;
        this.executionTimeMs = executionTimeMs;
    }

    public void transitionToFailed() {
        if (this.status != RagJobStatus.GENERATING && this.status != RagJobStatus.RETRIEVING && this.status != RagJobStatus.CONSTRUCTING_CONTEXT) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to FAILED");
        }
        this.status = RagJobStatus.FAILED;
    }

    // Getters
    public UUID getJobId() { return jobId; }
    public String getTenantId() { return tenantId; }
    public UUID getUserId() { return userId; }
    public String getQuery() { return query; }
    public String getPromptTemplate() { return promptTemplate; }
    public int getTokenBudget() { return tokenBudget; }
    public RagJobStatus getStatus() { return status; }
    public String getResponseText() { return responseText; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public Integer getCitationCount() { return citationCount; }
    public Instant getCreatedAt() { return createdAt; }
}
