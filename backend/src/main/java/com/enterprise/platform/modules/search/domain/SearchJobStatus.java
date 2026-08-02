package com.enterprise.platform.modules.search.domain;

public enum SearchJobStatus {
    PENDING,
    INDEXING,
    COMPLETED,
    FAILED,
    RETRYING,
    SKIPPED
}
