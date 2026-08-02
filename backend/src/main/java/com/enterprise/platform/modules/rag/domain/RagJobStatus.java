package com.enterprise.platform.modules.rag.domain;

public enum RagJobStatus {
    RECEIVED,
    RETRIEVING,
    CONSTRUCTING_CONTEXT,
    GENERATING,
    COMPLETED,
    FAILED
}
