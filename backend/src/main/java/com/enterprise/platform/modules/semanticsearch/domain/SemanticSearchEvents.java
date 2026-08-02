package com.enterprise.platform.modules.semanticsearch.domain;

import java.time.Instant;
import java.util.UUID;

public final class SemanticSearchEvents {

    private SemanticSearchEvents() {}

    public static abstract class BaseSemanticSearchEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID jobId;
        private final String tenantId;
        private final String provider;
        private final String embeddingModel;
        private final String similarityMetric;
        private final boolean cacheHit;
        private final long executionTime;
        private final int resultCount;
        private final Instant timestamp;

        protected BaseSemanticSearchEvent(UUID jobId, String tenantId, String provider, String embeddingModel,
                                          String similarityMetric, boolean cacheHit, long executionTime, int resultCount) {
            this.jobId = jobId;
            this.tenantId = tenantId;
            this.provider = provider;
            this.embeddingModel = embeddingModel;
            this.similarityMetric = similarityMetric;
            this.cacheHit = cacheHit;
            this.executionTime = executionTime;
            this.resultCount = resultCount;
            this.timestamp = Instant.now();
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getJobId() { return jobId; }
        public String getProvider() { return provider; }
        public String getEmbeddingModel() { return embeddingModel; }
        public String getSimilarityMetric() { return similarityMetric; }
        public boolean isCacheHit() { return cacheHit; }
        public long getExecutionTime() { return executionTime; }
        public int getResultCount() { return resultCount; }
        public Instant getTimestamp() { return timestamp; }

        @Override public String getTenantId() { return tenantId; }
        @Override public String getEntityType() { return "SEMANTIC_SEARCH_JOB"; }
        @Override public String getEntityId() { return jobId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Requested")) return "REQUEST";
            if (name.contains("Generated")) return "GENERATE_EMBEDDING";
            if (name.contains("Completed")) return "COMPLETE";
            if (name.contains("Failed")) return "FAIL";
            return "UNKNOWN";
        }
    }

    public static class SemanticSearchRequestedEvent extends BaseSemanticSearchEvent {
        public SemanticSearchRequestedEvent(UUID jobId, String tenantId, String provider, String embeddingModel, String similarityMetric) {
            super(jobId, tenantId, provider, embeddingModel, similarityMetric, false, 0, 0);
        }
    }

    public static class QueryEmbeddingGeneratedEvent extends BaseSemanticSearchEvent {
        public QueryEmbeddingGeneratedEvent(UUID jobId, String tenantId, String provider, String embeddingModel, String similarityMetric) {
            super(jobId, tenantId, provider, embeddingModel, similarityMetric, false, 0, 0);
        }
    }

    public static class SemanticSearchCompletedEvent extends BaseSemanticSearchEvent {
        public SemanticSearchCompletedEvent(UUID jobId, String tenantId, String provider, String embeddingModel,
                                            String similarityMetric, boolean cacheHit, long executionTime, int resultCount) {
            super(jobId, tenantId, provider, embeddingModel, similarityMetric, cacheHit, executionTime, resultCount);
        }
    }

    public static class SemanticSearchFailedEvent extends BaseSemanticSearchEvent {
        private final String errorMessage;

        public SemanticSearchFailedEvent(UUID jobId, String tenantId, String provider, String embeddingModel,
                                         String similarityMetric, String errorMessage) {
            super(jobId, tenantId, provider, embeddingModel, similarityMetric, false, 0, 0);
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() { return errorMessage; }
    }
}
