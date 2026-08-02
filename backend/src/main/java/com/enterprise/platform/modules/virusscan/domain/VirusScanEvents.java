package com.enterprise.platform.modules.virusscan.domain;

import java.time.Instant;
import java.util.UUID;

public final class VirusScanEvents {

    private VirusScanEvents() {}

    public abstract static class BaseScanEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID scanJobId;
        private final UUID documentId;
        private final UUID versionId;
        private final String tenantId;
        private final UUID userId;
        private final Instant timestamp = Instant.now();

        protected BaseScanEvent(UUID scanJobId, UUID documentId, UUID versionId, String tenantId, UUID userId) {
            this.scanJobId = scanJobId;
            this.documentId = documentId;
            this.versionId = versionId;
            this.tenantId = tenantId;
            this.userId = userId;
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getScanJobId() { return scanJobId; }
        public UUID getDocumentId() { return documentId; }
        public UUID getVersionId() { return versionId; }
        public String getTenantId() { return tenantId; }
        public UUID getUserId() { return userId; }
        public Instant getTimestamp() { return timestamp; }
        @Override public String getEntityType() { return "VIRUS_SCAN_JOB"; }
        @Override public String getEntityId() { return scanJobId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Requested")) return "REQUEST";
            if (name.contains("Started")) return "START";
            if (name.contains("Completed")) return "COMPLETE";
            if (name.contains("Failed")) return "FAIL";
            if (name.contains("Retried")) return "RETRY";
            if (name.contains("Quarantined")) return "QUARANTINE";
            return "UNKNOWN";
        }
    }

    public static class VirusScanRequestedEvent extends BaseScanEvent {
        public VirusScanRequestedEvent(UUID scanJobId, UUID documentId, UUID versionId, String tenantId, UUID userId) {
            super(scanJobId, documentId, versionId, tenantId, userId);
        }
    }

    public static class VirusScanStartedEvent extends BaseScanEvent {
        public VirusScanStartedEvent(UUID scanJobId, UUID documentId, UUID versionId, String tenantId, UUID userId) {
            super(scanJobId, documentId, versionId, tenantId, userId);
        }
    }

    public static class VirusScanCompletedEvent extends BaseScanEvent {
        public VirusScanCompletedEvent(UUID scanJobId, UUID documentId, UUID versionId, String tenantId, UUID userId) {
            super(scanJobId, documentId, versionId, tenantId, userId);
        }
    }

    public static class VirusScanFailedEvent extends BaseScanEvent {
        public VirusScanFailedEvent(UUID scanJobId, UUID documentId, UUID versionId, String tenantId, UUID userId) {
            super(scanJobId, documentId, versionId, tenantId, userId);
        }
    }

    public static class VirusScanRetriedEvent extends BaseScanEvent {
        public VirusScanRetriedEvent(UUID scanJobId, UUID documentId, UUID versionId, String tenantId, UUID userId) {
            super(scanJobId, documentId, versionId, tenantId, userId);
        }
    }

    public static class DocumentQuarantinedEvent extends BaseScanEvent {
        public DocumentQuarantinedEvent(UUID scanJobId, UUID documentId, UUID versionId, String tenantId, UUID userId) {
            super(scanJobId, documentId, versionId, tenantId, userId);
        }
    }
}
