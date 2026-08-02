package com.enterprise.platform.modules.embedding.domain;

import java.time.Instant;
import java.util.UUID;

public final class EmbeddingEvents {

    private EmbeddingEvents() {}

    public static abstract class BaseEmbeddingEvent {
        private final int eventVersion = 1;
        private final UUID jobId;
        private final UUID documentId;
        private final UUID versionId;
        private final String tenantId;
        private final String provider;
        private final String embeddingModel;
        private final String embeddingModelVersion;
        private final int chunkCount;
        private final Instant timestamp;

        protected BaseEmbeddingEvent(UUID jobId, UUID documentId, UUID versionId, String tenantId, 
                                     String provider, String embeddingModel, String embeddingModelVersion, 
                                     int chunkCount) {
            this.jobId = jobId;
            this.documentId = documentId;
            this.versionId = versionId;
            this.tenantId = tenantId;
            this.provider = provider;
            this.embeddingModel = embeddingModel;
            this.embeddingModelVersion = embeddingModelVersion;
            this.chunkCount = chunkCount;
            this.timestamp = Instant.now();
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getJobId() { return jobId; }
        public UUID getDocumentId() { return documentId; }
        public UUID getVersionId() { return versionId; }
        public String getTenantId() { return tenantId; }
        public String getProvider() { return provider; }
        public String getEmbeddingModel() { return embeddingModel; }
        public String getEmbeddingModelVersion() { return embeddingModelVersion; }
        public int getChunkCount() { return chunkCount; }
        public Instant getTimestamp() { return timestamp; }
    }

    public static class EmbeddingGenerationRequestedEvent extends BaseEmbeddingEvent {
        public EmbeddingGenerationRequestedEvent(UUID jobId, UUID documentId, UUID versionId, String tenantId, 
                                                 String provider, String embeddingModel, String embeddingModelVersion) {
            super(jobId, documentId, versionId, tenantId, provider, embeddingModel, embeddingModelVersion, 0);
        }
    }

    public static class EmbeddingGenerationStartedEvent extends BaseEmbeddingEvent {
        public EmbeddingGenerationStartedEvent(UUID jobId, UUID documentId, UUID versionId, String tenantId, 
                                               String provider, String embeddingModel, String embeddingModelVersion) {
            super(jobId, documentId, versionId, tenantId, provider, embeddingModel, embeddingModelVersion, 0);
        }
    }

    public static class EmbeddingCompletedEvent extends BaseEmbeddingEvent {
        public EmbeddingCompletedEvent(UUID jobId, UUID documentId, UUID versionId, String tenantId, 
                                       String provider, String embeddingModel, String embeddingModelVersion, int chunkCount) {
            super(jobId, documentId, versionId, tenantId, provider, embeddingModel, embeddingModelVersion, chunkCount);
        }
    }

    public static class EmbeddingSkippedEvent extends BaseEmbeddingEvent {
        private final String reason;

        public EmbeddingSkippedEvent(UUID jobId, UUID documentId, UUID versionId, String tenantId, 
                                     String provider, String embeddingModel, String embeddingModelVersion, String reason) {
            super(jobId, documentId, versionId, tenantId, provider, embeddingModel, embeddingModelVersion, 0);
            this.reason = reason;
        }

        public String getReason() { return reason; }
    }

    public static class EmbeddingFailedEvent extends BaseEmbeddingEvent {
        private final String errorMessage;

        public EmbeddingFailedEvent(UUID jobId, UUID documentId, UUID versionId, String tenantId, 
                                    String provider, String embeddingModel, String embeddingModelVersion, String errorMessage) {
            super(jobId, documentId, versionId, tenantId, provider, embeddingModel, embeddingModelVersion, 0);
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() { return errorMessage; }
    }

    public static class EmbeddingRetriedEvent extends BaseEmbeddingEvent {
        private final int attempt;

        public EmbeddingRetriedEvent(UUID jobId, UUID documentId, UUID versionId, String tenantId, 
                                     String provider, String embeddingModel, String embeddingModelVersion, int attempt) {
            super(jobId, documentId, versionId, tenantId, provider, embeddingModel, embeddingModelVersion, 0);
            this.attempt = attempt;
        }

        public int getAttempt() { return attempt; }
    }
}
