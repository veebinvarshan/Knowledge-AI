package com.enterprise.platform.modules.documents.domain;

import java.util.UUID;

public final class FolderEvents {

    private FolderEvents() {}

    public static class FolderCreatedEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final UUID folderId;
        private final String name;
        private final String tenantId;
        private final UUID createdBy;

        public FolderCreatedEvent(UUID folderId, String name, String tenantId, UUID createdBy) {
            this.folderId = folderId;
            this.name = name;
            this.tenantId = tenantId;
            this.createdBy = createdBy;
        }

        public UUID getFolderId() { return folderId; }
        public String getName() { return name; }
        public String getTenantId() { return tenantId; }
        public UUID getCreatedBy() { return createdBy; }

        @Override public UUID getUserId() { return createdBy; }
        @Override public String getEntityType() { return "FOLDER"; }
        @Override public String getEntityId() { return folderId.toString(); }
        @Override public String getAction() { return "CREATE"; }
    }

    public static class FolderRenamedEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final UUID folderId;
        private final String oldName;
        private final String newName;
        private final String tenantId;
        private final UUID updatedBy;

        public FolderRenamedEvent(UUID folderId, String oldName, String newName, String tenantId, UUID updatedBy) {
            this.folderId = folderId;
            this.oldName = oldName;
            this.newName = newName;
            this.tenantId = tenantId;
            this.updatedBy = updatedBy;
        }

        public UUID getFolderId() { return folderId; }
        public String getOldName() { return oldName; }
        public String getNewName() { return newName; }
        public String getTenantId() { return tenantId; }
        public UUID getUpdatedBy() { return updatedBy; }

        @Override public UUID getUserId() { return updatedBy; }
        @Override public String getEntityType() { return "FOLDER"; }
        @Override public String getEntityId() { return folderId.toString(); }
        @Override public String getAction() { return "RENAME"; }
    }

    public static class FolderMovedEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final UUID folderId;
        private final UUID oldParentId;
        private final UUID newParentId;
        private final String tenantId;
        private final UUID updatedBy;

        public FolderMovedEvent(UUID folderId, UUID oldParentId, UUID newParentId, String tenantId, UUID updatedBy) {
            this.folderId = folderId;
            this.oldParentId = oldParentId;
            this.newParentId = newParentId;
            this.tenantId = tenantId;
            this.updatedBy = updatedBy;
        }

        public UUID getFolderId() { return folderId; }
        public UUID getOldParentId() { return oldParentId; }
        public UUID getNewParentId() { return newParentId; }
        public String getTenantId() { return tenantId; }
        public UUID getUpdatedBy() { return updatedBy; }

        @Override public UUID getUserId() { return updatedBy; }
        @Override public String getEntityType() { return "FOLDER"; }
        @Override public String getEntityId() { return folderId.toString(); }
        @Override public String getAction() { return "MOVE"; }
    }

    public static class FolderDeletedEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final UUID folderId;
        private final String tenantId;
        private final UUID deletedBy;

        public FolderDeletedEvent(UUID folderId, String tenantId, UUID deletedBy) {
            this.folderId = folderId;
            this.tenantId = tenantId;
            this.deletedBy = deletedBy;
        }

        public UUID getFolderId() { return folderId; }
        public String getTenantId() { return tenantId; }
        public UUID getDeletedBy() { return deletedBy; }

        @Override public UUID getUserId() { return deletedBy; }
        @Override public String getEntityType() { return "FOLDER"; }
        @Override public String getEntityId() { return folderId.toString(); }
        @Override public String getAction() { return "DELETE"; }
    }

    public static class FolderRestoredEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final UUID folderId;
        private final String tenantId;
        private final UUID restoredBy;

        public FolderRestoredEvent(UUID folderId, String tenantId, UUID restoredBy) {
            this.folderId = folderId;
            this.tenantId = tenantId;
            this.restoredBy = restoredBy;
        }

        public UUID getFolderId() { return folderId; }
        public String getTenantId() { return tenantId; }
        public UUID getRestoredBy() { return restoredBy; }

        @Override public UUID getUserId() { return restoredBy; }
        @Override public String getEntityType() { return "FOLDER"; }
        @Override public String getEntityId() { return folderId.toString(); }
        @Override public String getAction() { return "RESTORE"; }
    }

    public static class FolderArchivedEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final UUID folderId;
        private final String tenantId;
        private final UUID archivedBy;

        public FolderArchivedEvent(UUID folderId, String tenantId, UUID archivedBy) {
            this.folderId = folderId;
            this.tenantId = tenantId;
            this.archivedBy = archivedBy;
        }

        public UUID getFolderId() { return folderId; }
        public String getTenantId() { return tenantId; }
        public UUID getArchivedBy() { return archivedBy; }

        @Override public UUID getUserId() { return archivedBy; }
        @Override public String getEntityType() { return "FOLDER"; }
        @Override public String getEntityId() { return folderId.toString(); }
        @Override public String getAction() { return "ARCHIVE"; }
    }
}
