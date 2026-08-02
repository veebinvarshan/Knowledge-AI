package com.enterprise.platform.modules.metadata.domain;

import java.time.Instant;
import java.util.UUID;

public final class MetadataEvents {

    private MetadataEvents() {}

    public abstract static class BaseMetadataEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID metadataJobId;
        private final UUID documentId;
        private final UUID versionId;
        private final String tenantId;
        private final String extractorProvider;
        private final Instant timestamp = Instant.now();

        protected BaseMetadataEvent(UUID metadataJobId, UUID documentId, UUID versionId, String tenantId, String extractorProvider) {
            this.metadataJobId = metadataJobId;
            this.documentId = documentId;
            this.versionId = versionId;
            this.tenantId = tenantId;
            this.extractorProvider = extractorProvider;
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getMetadataJobId() { return metadataJobId; }
        public UUID getDocumentId() { return documentId; }
        public UUID getVersionId() { return versionId; }
        public String getTenantId() { return tenantId; }
        public String getExtractorProvider() { return extractorProvider; }
        public Instant getTimestamp() { return timestamp; }
        @Override public String getEntityType() { return "METADATA_EXTRACTION_JOB"; }
        @Override public String getEntityId() { return metadataJobId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Requested")) return "REQUEST";
            if (name.contains("Started")) return "START";
            if (name.contains("Completed")) return "COMPLETE";
            if (name.contains("Failed")) return "FAIL";
            if (name.contains("Retried")) return "RETRY";
            return "UNKNOWN";
        }
    }

    public static class MetadataExtractionRequestedEvent extends BaseMetadataEvent {
        public MetadataExtractionRequestedEvent(UUID metadataJobId, UUID documentId, UUID versionId, String tenantId, String extractorProvider) {
            super(metadataJobId, documentId, versionId, tenantId, extractorProvider);
        }
    }

    public static class MetadataExtractionStartedEvent extends BaseMetadataEvent {
        public MetadataExtractionStartedEvent(UUID metadataJobId, UUID documentId, UUID versionId, String tenantId, String extractorProvider) {
            super(metadataJobId, documentId, versionId, tenantId, extractorProvider);
        }
    }

    public static class MetadataExtractionCompletedEvent extends BaseMetadataEvent {
        public MetadataExtractionCompletedEvent(UUID metadataJobId, UUID documentId, UUID versionId, String tenantId, String extractorProvider) {
            super(metadataJobId, documentId, versionId, tenantId, extractorProvider);
        }
    }

    public static class MetadataExtractionFailedEvent extends BaseMetadataEvent {
        public MetadataExtractionFailedEvent(UUID metadataJobId, UUID documentId, UUID versionId, String tenantId, String extractorProvider) {
            super(metadataJobId, documentId, versionId, tenantId, extractorProvider);
        }
    }

    public static class MetadataExtractionRetriedEvent extends BaseMetadataEvent {
        public MetadataExtractionRetriedEvent(UUID metadataJobId, UUID documentId, UUID versionId, String tenantId, String extractorProvider) {
            super(metadataJobId, documentId, versionId, tenantId, extractorProvider);
        }
    }
}
