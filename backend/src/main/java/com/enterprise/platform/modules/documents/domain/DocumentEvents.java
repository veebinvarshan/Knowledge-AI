package com.enterprise.platform.modules.documents.domain;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class DocumentEvents {

    private DocumentEvents() {}

    public abstract static class BaseDocumentEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID eventId = UUID.randomUUID();
        private final Instant occurredAt = Instant.now();
        private final UUID documentId;
        private final String tenantId;

        protected BaseDocumentEvent(UUID documentId, String tenantId) {
            this.documentId = documentId;
            this.tenantId = tenantId;
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getEventId() { return eventId; }
        public Instant getOccurredAt() { return occurredAt; }
        public UUID getDocumentId() { return documentId; }
        public String getTenantId() { return tenantId; }

        @Override public String getEntityType() { return "DOCUMENT"; }
        @Override public String getEntityId() { return documentId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Created")) return "CREATE";
            if (name.contains("Renamed")) return "RENAME";
            if (name.contains("Moved")) return "MOVE";
            if (name.contains("Archived")) return "ARCHIVE";
            if (name.contains("Restored")) return "RESTORE";
            if (name.contains("Deleted")) return "DELETE";
            if (name.contains("Ownership")) return "OWNERSHIP_CHANGE";
            if (name.contains("Metadata")) return "METADATA_UPDATE";
            if (name.contains("Tag")) return "TAG_UPDATE";
            return "UNKNOWN";
        }
    }

    public static class DocumentCreatedEvent extends BaseDocumentEvent {
        private final String title;
        private final UUID ownerId;

        public DocumentCreatedEvent(UUID documentId, String tenantId, String title, UUID ownerId) {
            super(documentId, tenantId);
            this.title = title;
            this.ownerId = ownerId;
        }

        public String getTitle() { return title; }
        public UUID getOwnerId() { return ownerId; }

        @Override public UUID getUserId() { return ownerId; }
    }

    public static class DocumentRenamedEvent extends BaseDocumentEvent {
        private final String oldTitle;
        private final String newTitle;

        public DocumentRenamedEvent(UUID documentId, String tenantId, String oldTitle, String newTitle) {
            super(documentId, tenantId);
            this.oldTitle = oldTitle;
            this.newTitle = newTitle;
        }

        public String getOldTitle() { return oldTitle; }
        public String getNewTitle() { return newTitle; }
    }

    public static class DocumentMovedEvent extends BaseDocumentEvent {
        private final UUID oldFolderId;
        private final UUID newFolderId;

        public DocumentMovedEvent(UUID documentId, String tenantId, UUID oldFolderId, UUID newFolderId) {
            super(documentId, tenantId);
            this.oldFolderId = oldFolderId;
            this.newFolderId = newFolderId;
        }

        public UUID getOldFolderId() { return oldFolderId; }
        public UUID getNewFolderId() { return newFolderId; }
    }

    public static class DocumentArchivedEvent extends BaseDocumentEvent {
        public DocumentArchivedEvent(UUID documentId, String tenantId) {
            super(documentId, tenantId);
        }
    }

    public static class DocumentRestoredEvent extends BaseDocumentEvent {
        public DocumentRestoredEvent(UUID documentId, String tenantId) {
            super(documentId, tenantId);
        }
    }

    public static class DocumentDeletedEvent extends BaseDocumentEvent {
        public DocumentDeletedEvent(UUID documentId, String tenantId) {
            super(documentId, tenantId);
        }
    }

    public static class DocumentOwnershipChangedEvent extends BaseDocumentEvent {
        private final UUID oldOwnerId;
        private final UUID newOwnerId;

        public DocumentOwnershipChangedEvent(UUID documentId, String tenantId, UUID oldOwnerId, UUID newOwnerId) {
            super(documentId, tenantId);
            this.oldOwnerId = oldOwnerId;
            this.newOwnerId = newOwnerId;
        }

        public UUID getOldOwnerId() { return oldOwnerId; }
        public UUID getNewOwnerId() { return newOwnerId; }
    }

    public static class DocumentMetadataUpdatedEvent extends BaseDocumentEvent {
        private final Set<String> updatedKeys;

        public DocumentMetadataUpdatedEvent(UUID documentId, String tenantId, Set<String> updatedKeys) {
            super(documentId, tenantId);
            this.updatedKeys = updatedKeys;
        }

        public Set<String> getUpdatedKeys() { return updatedKeys; }
    }

    public static class DocumentTagAssignedEvent extends BaseDocumentEvent {
        private final Set<UUID> tagIds;

        public DocumentTagAssignedEvent(UUID documentId, String tenantId, Set<UUID> tagIds) {
            super(documentId, tenantId);
            this.tagIds = tagIds;
        }

        public Set<UUID> getTagIds() { return tagIds; }
    }

    public static class DocumentTagRemovedEvent extends BaseDocumentEvent {
        private final Set<UUID> tagIds;

        public DocumentTagRemovedEvent(UUID documentId, String tenantId, Set<UUID> tagIds) {
            super(documentId, tenantId);
            this.tagIds = tagIds;
        }

        public Set<UUID> getTagIds() { return tagIds; }
    }
}
