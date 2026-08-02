package com.enterprise.platform.modules.documents.domain;

import java.time.Instant;
import java.util.UUID;

public final class DocumentVersionEvents {

    private DocumentVersionEvents() {}

    public abstract static class BaseVersionEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID documentId;
        private final UUID versionId;
        private final int versionNumber;
        private final UUID storageObjectId;
        private final String checksum;
        private final String tenantId;
        private final UUID userId;
        private final Instant timestamp = Instant.now();

        protected BaseVersionEvent(UUID documentId, UUID versionId, int versionNumber, 
                                   UUID storageObjectId, String checksum, String tenantId, UUID userId) {
            this.documentId = documentId;
            this.versionId = versionId;
            this.versionNumber = versionNumber;
            this.storageObjectId = storageObjectId;
            this.checksum = checksum;
            this.tenantId = tenantId;
            this.userId = userId;
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getDocumentId() { return documentId; }
        public UUID getVersionId() { return versionId; }
        public int getVersionNumber() { return versionNumber; }
        public UUID getStorageObjectId() { return storageObjectId; }
        public String getChecksum() { return checksum; }
        public String getTenantId() { return tenantId; }
        public UUID getUserId() { return userId; }
        public Instant getTimestamp() { return timestamp; }
        @Override public String getEntityType() { return "DOCUMENT_VERSION"; }
        @Override public String getEntityId() { return versionId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Created")) return "CREATE";
            if (name.contains("Superseded")) return "SUPERSEDE";
            if (name.contains("Archived")) return "ARCHIVE";
            return "UNKNOWN";
        }
    }

    public static class DocumentVersionCreatedEvent extends BaseVersionEvent {
        public DocumentVersionCreatedEvent(UUID documentId, UUID versionId, int versionNumber, 
                                           UUID storageObjectId, String checksum, String tenantId, UUID userId) {
            super(documentId, versionId, versionNumber, storageObjectId, checksum, tenantId, userId);
        }
    }

    public static class DocumentVersionSupersededEvent extends BaseVersionEvent {
        public DocumentVersionSupersededEvent(UUID documentId, UUID versionId, int versionNumber, 
                                              UUID storageObjectId, String checksum, String tenantId, UUID userId) {
            super(documentId, versionId, versionNumber, storageObjectId, checksum, tenantId, userId);
        }
    }

    public static class DocumentVersionArchivedEvent extends BaseVersionEvent {
        public DocumentVersionArchivedEvent(UUID documentId, UUID versionId, int versionNumber, 
                                            UUID storageObjectId, String checksum, String tenantId, UUID userId) {
            super(documentId, versionId, versionNumber, storageObjectId, checksum, tenantId, userId);
        }
    }
}
