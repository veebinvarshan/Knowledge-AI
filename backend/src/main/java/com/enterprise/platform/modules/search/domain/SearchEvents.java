package com.enterprise.platform.modules.search.domain;

import java.time.Instant;
import java.util.UUID;

public final class SearchEvents {

    private SearchEvents() {}

    public abstract static class BaseSearchEvent implements com.enterprise.platform.core.audit.AuditableEvent {
        private final int eventVersion = 1;
        private final UUID searchJobId;
        private final UUID documentId;
        private final UUID versionId;
        private final String tenantId;
        private final String indexType;
        private final String provider;
        private final Instant timestamp = Instant.now();

        protected BaseSearchEvent(UUID searchJobId, UUID documentId, UUID versionId, String tenantId, String indexType, String provider) {
            this.searchJobId = searchJobId;
            this.documentId = documentId;
            this.versionId = versionId;
            this.tenantId = tenantId;
            this.indexType = indexType;
            this.provider = provider;
        }

        public int getEventVersion() { return eventVersion; }
        public UUID getSearchJobId() { return searchJobId; }
        public UUID getDocumentId() { return documentId; }
        public UUID getVersionId() { return versionId; }
        public String getTenantId() { return tenantId; }
        public String getIndexType() { return indexType; }
        public String getProvider() { return provider; }
        public Instant getTimestamp() { return timestamp; }
        @Override public String getEntityType() { return "SEARCH_INDEX_JOB"; }
        @Override public String getEntityId() { return searchJobId.toString(); }
        @Override public String getAction() {
            String name = getClass().getSimpleName();
            if (name.contains("Requested")) return "REQUEST";
            if (name.contains("Started")) return "START";
            if (name.contains("Completed")) return "COMPLETE";
            if (name.contains("Indexed")) return "INDEX";
            if (name.contains("Skipped")) return "SKIP";
            if (name.contains("Failed")) return "FAIL";
            if (name.contains("Retried")) return "RETRY";
            return "UNKNOWN";
        }
    }

    public static class SearchIndexRequestedEvent extends BaseSearchEvent {
        public SearchIndexRequestedEvent(UUID searchJobId, UUID documentId, UUID versionId, String tenantId, String indexType, String provider) {
            super(searchJobId, documentId, versionId, tenantId, indexType, provider);
        }
    }

    public static class SearchIndexStartedEvent extends BaseSearchEvent {
        public SearchIndexStartedEvent(UUID searchJobId, UUID documentId, UUID versionId, String tenantId, String indexType, String provider) {
            super(searchJobId, documentId, versionId, tenantId, indexType, provider);
        }
    }

    public static class SearchIndexedEvent extends BaseSearchEvent {
        public SearchIndexedEvent(UUID searchJobId, UUID documentId, UUID versionId, String tenantId, String indexType, String provider) {
            super(searchJobId, documentId, versionId, tenantId, indexType, provider);
        }
    }

    public static class SearchIndexSkippedEvent extends BaseSearchEvent {
        public SearchIndexSkippedEvent(UUID searchJobId, UUID documentId, UUID versionId, String tenantId, String indexType, String provider) {
            super(searchJobId, documentId, versionId, tenantId, indexType, provider);
        }
    }

    public static class SearchIndexFailedEvent extends BaseSearchEvent {
        public SearchIndexFailedEvent(UUID searchJobId, UUID documentId, UUID versionId, String tenantId, String indexType, String provider) {
            super(searchJobId, documentId, versionId, tenantId, indexType, provider);
        }
    }

    public static class SearchIndexRetriedEvent extends BaseSearchEvent {
        public SearchIndexRetriedEvent(UUID searchJobId, UUID documentId, UUID versionId, String tenantId, String indexType, String provider) {
            super(searchJobId, documentId, versionId, tenantId, indexType, provider);
        }
    }
}
