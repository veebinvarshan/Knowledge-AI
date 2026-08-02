package com.enterprise.platform.modules.ocr.domain;

import java.time.Instant;
import java.util.UUID;

public final class OcrEvents {

    private OcrEvents() {}

    public abstract static class BaseOcrEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID ocrJobId;
        private final UUID documentId;
        private final UUID versionId;
        private final String tenantId;
        private final String provider;
        private final String language;
        private final Instant timestamp = Instant.now();

        protected BaseOcrEvent(UUID ocrJobId, UUID documentId, UUID versionId, String tenantId, String provider, String language) {
            this.ocrJobId = ocrJobId;
            this.documentId = documentId;
            this.versionId = versionId;
            this.tenantId = tenantId;
            this.provider = provider;
            this.language = language;
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getOcrJobId() { return ocrJobId; }
        public UUID getDocumentId() { return documentId; }
        public UUID getVersionId() { return versionId; }
        public String getTenantId() { return tenantId; }
        public String getProvider() { return provider; }
        public String getLanguage() { return language; }
        public Instant getTimestamp() { return timestamp; }
        @Override public String getEntityType() { return "OCR_JOB"; }
        @Override public String getEntityId() { return ocrJobId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Requested")) return "REQUEST";
            if (name.contains("Started")) return "START";
            if (name.contains("Completed")) return "COMPLETE";
            if (name.contains("Skipped")) return "SKIP";
            if (name.contains("Failed")) return "FAIL";
            if (name.contains("Retried")) return "RETRY";
            return "UNKNOWN";
        }
    }

    public static class OcrRequestedEvent extends BaseOcrEvent {
        public OcrRequestedEvent(UUID ocrJobId, UUID documentId, UUID versionId, String tenantId, String provider, String language) {
            super(ocrJobId, documentId, versionId, tenantId, provider, language);
        }
    }

    public static class OcrStartedEvent extends BaseOcrEvent {
        public OcrStartedEvent(UUID ocrJobId, UUID documentId, UUID versionId, String tenantId, String provider, String language) {
            super(ocrJobId, documentId, versionId, tenantId, provider, language);
        }
    }

    public static class OcrCompletedEvent extends BaseOcrEvent {
        public OcrCompletedEvent(UUID ocrJobId, UUID documentId, UUID versionId, String tenantId, String provider, String language) {
            super(ocrJobId, documentId, versionId, tenantId, provider, language);
        }
    }

    public static class OcrSkippedEvent extends BaseOcrEvent {
        private final String reason;

        public OcrSkippedEvent(UUID ocrJobId, UUID documentId, UUID versionId, String tenantId, String provider, String language, String reason) {
            super(ocrJobId, documentId, versionId, tenantId, provider, language);
            this.reason = reason;
        }

        public String getReason() { return reason; }
    }

    public static class OcrFailedEvent extends BaseOcrEvent {
        public OcrFailedEvent(UUID ocrJobId, UUID documentId, UUID versionId, String tenantId, String provider, String language) {
            super(ocrJobId, documentId, versionId, tenantId, provider, language);
        }
    }

    public static class OcrRetriedEvent extends BaseOcrEvent {
        public OcrRetriedEvent(UUID ocrJobId, UUID documentId, UUID versionId, String tenantId, String provider, String language) {
            super(ocrJobId, documentId, versionId, tenantId, provider, language);
        }
    }
}
