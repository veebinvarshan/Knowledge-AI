package com.enterprise.platform.modules.storage.domain;

import java.time.Instant;
import java.util.UUID;

public final class StorageEvents {

    private StorageEvents() {}

    public abstract static class BaseStorageEvent {
        private final int eventVersion = 1;
        private final UUID objectId;
        private final String provider;
        private final String logicalPath;
        private final Instant timestamp = Instant.now();

        protected BaseStorageEvent(UUID objectId, String provider, String logicalPath) {
            this.objectId = objectId;
            this.provider = provider;
            this.logicalPath = logicalPath;
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getObjectId() { return objectId; }
        public String getProvider() { return provider; }
        public String getLogicalPath() { return logicalPath; }
        public Instant getTimestamp() { return timestamp; }
    }

    public static class StorageObjectCreatedEvent extends BaseStorageEvent {
        public StorageObjectCreatedEvent(UUID objectId, String provider, String logicalPath) {
            super(objectId, provider, logicalPath);
        }
    }

    public static class StorageObjectDeletedEvent extends BaseStorageEvent {
        public StorageObjectDeletedEvent(UUID objectId, String provider, String logicalPath) {
            super(objectId, provider, logicalPath);
        }
    }

    public static class StorageObjectMovedEvent extends BaseStorageEvent {
        private final String oldLogicalPath;

        public StorageObjectMovedEvent(UUID objectId, String provider, String oldLogicalPath, String newLogicalPath) {
            super(objectId, provider, newLogicalPath);
            this.oldLogicalPath = oldLogicalPath;
        }

        public String getOldLogicalPath() { return oldLogicalPath; }
    }

    public static class StorageObjectCopiedEvent extends BaseStorageEvent {
        private final String sourceLogicalPath;

        public StorageObjectCopiedEvent(UUID objectId, String provider, String sourceLogicalPath, String destLogicalPath) {
            super(objectId, provider, destLogicalPath);
            this.sourceLogicalPath = sourceLogicalPath;
        }

        public String getSourceLogicalPath() { return sourceLogicalPath; }
    }
}
