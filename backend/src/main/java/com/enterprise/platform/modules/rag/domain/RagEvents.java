package com.enterprise.platform.modules.rag.domain;

import java.time.Instant;
import java.util.UUID;

public final class RagEvents {

    private RagEvents() {}

    public static abstract class BaseRagEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID jobId;
        private final String tenantId;
        private final Instant timestamp;

        protected BaseRagEvent(UUID jobId, String tenantId) {
            this.jobId = jobId;
            this.tenantId = tenantId;
            this.timestamp = Instant.now();
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getJobId() { return jobId; }
        public String getTenantId() { return tenantId; }
        public Instant getTimestamp() { return timestamp; }
        @Override public String getEntityType() { return "RAG_JOB"; }
        @Override public String getEntityId() { return jobId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Requested")) return "REQUEST";
            if (name.contains("Built")) return "BUILD_CONTEXT";
            if (name.contains("Completed")) return "COMPLETE";
            if (name.contains("Failed")) return "FAIL";
            return "UNKNOWN";
        }
    }

    public static class RagJobRequestedEvent extends BaseRagEvent {
        public RagJobRequestedEvent(UUID jobId, String tenantId) {
            super(jobId, tenantId);
        }
    }

    public static class RagContextBuiltEvent extends BaseRagEvent {
        private final int citationCount;

        public RagContextBuiltEvent(UUID jobId, String tenantId, int citationCount) {
            super(jobId, tenantId);
            this.citationCount = citationCount;
        }

        public int getCitationCount() { return citationCount; }
    }

    public static class RagGenerationCompletedEvent extends BaseRagEvent {
        private final long executionTimeMs;
        private final int citationCount;

        public RagGenerationCompletedEvent(UUID jobId, String tenantId, long executionTimeMs, int citationCount) {
            super(jobId, tenantId);
            this.executionTimeMs = executionTimeMs;
            this.citationCount = citationCount;
        }

        public long getExecutionTimeMs() { return executionTimeMs; }
        public int getCitationCount() { return citationCount; }
    }

    public static class RagGenerationFailedEvent extends BaseRagEvent {
        private final String error;

        public RagGenerationFailedEvent(UUID jobId, String tenantId, String error) {
            super(jobId, tenantId);
            this.error = error;
        }

        public String getError() { return error; }
    }
}
