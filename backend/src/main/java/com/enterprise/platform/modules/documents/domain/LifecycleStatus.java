package com.enterprise.platform.modules.documents.domain;

public enum LifecycleStatus {
    DRAFT,
    UPLOADING,
    UPLOADED,
    VIRUS_SCAN,
    METADATA_EXTRACTION,
    VERSION_READY,
    READY,
    ARCHIVED,
    QUARANTINED
}
