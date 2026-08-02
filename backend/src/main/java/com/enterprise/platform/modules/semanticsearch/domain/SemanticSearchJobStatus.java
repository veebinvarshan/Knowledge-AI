package com.enterprise.platform.modules.semanticsearch.domain;

public enum SemanticSearchJobStatus {
    RECEIVED,
    EMBEDDING,
    SEARCHING,
    COMPLETED,
    FAILED,
    RETRYING
}
