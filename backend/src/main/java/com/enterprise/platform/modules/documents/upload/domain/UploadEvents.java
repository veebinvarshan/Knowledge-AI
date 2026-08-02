package com.enterprise.platform.modules.documents.upload.domain;

import java.time.Instant;
import java.util.UUID;

public final class UploadEvents {

    private UploadEvents() {}

    public abstract static class BaseUploadEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID uploadSessionId;
        private final String tenantId;
        private final UUID userId;
        private final Instant timestamp = Instant.now();

        protected BaseUploadEvent(UUID uploadSessionId, String tenantId, UUID userId) {
            this.uploadSessionId = uploadSessionId;
            this.tenantId = tenantId;
            this.userId = userId;
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getUploadSessionId() { return uploadSessionId; }
        public String getTenantId() { return tenantId; }
        public UUID getUserId() { return userId; }
        public Instant getTimestamp() { return timestamp; }
        @Override public String getEntityType() { return "UPLOAD_SESSION"; }
        @Override public String getEntityId() { return uploadSessionId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Initialized")) return "INITIALIZE";
            if (name.contains("ChunkUploaded")) return "CHUNK_UPLOAD";
            if (name.contains("Completed")) return "COMPLETE";
            if (name.contains("Failed")) return "FAIL";
            if (name.contains("Expired")) return "EXPIRE";
            if (name.contains("Aborted")) return "ABORT";
            return "UNKNOWN";
        }
    }

    public static class UploadSessionInitializedEvent extends BaseUploadEvent {
        public UploadSessionInitializedEvent(UUID uploadSessionId, String tenantId, UUID userId) {
            super(uploadSessionId, tenantId, userId);
        }
    }

    public static class UploadChunkUploadedEvent extends BaseUploadEvent {
        private final int chunkNumber;

        public UploadChunkUploadedEvent(UUID uploadSessionId, String tenantId, UUID userId, int chunkNumber) {
            super(uploadSessionId, tenantId, userId);
            this.chunkNumber = chunkNumber;
        }

        public int getChunkNumber() { return chunkNumber; }
    }

    public static class UploadSessionCompletedEvent extends BaseUploadEvent {
        private final String logicalPath;
        private final UUID storageObjectId;

        public UploadSessionCompletedEvent(UUID uploadSessionId, String tenantId, UUID userId, String logicalPath, UUID storageObjectId) {
            super(uploadSessionId, tenantId, userId);
            this.logicalPath = logicalPath;
            this.storageObjectId = storageObjectId;
        }

        public String getLogicalPath() { return logicalPath; }
        public UUID getStorageObjectId() { return storageObjectId; }
    }

    public static class UploadSessionFailedEvent extends BaseUploadEvent {
        private final String reason;

        public UploadSessionFailedEvent(UUID uploadSessionId, String tenantId, UUID userId, String reason) {
            super(uploadSessionId, tenantId, userId);
            this.reason = reason;
        }

        public String getReason() { return reason; }
    }

    public static class UploadSessionExpiredEvent extends BaseUploadEvent {
        public UploadSessionExpiredEvent(UUID uploadSessionId, String tenantId, UUID userId) {
            super(uploadSessionId, tenantId, userId);
        }
    }

    public static class UploadSessionAbortedEvent extends BaseUploadEvent {
        public UploadSessionAbortedEvent(UUID uploadSessionId, String tenantId, UUID userId) {
            super(uploadSessionId, tenantId, userId);
        }
    }
}
