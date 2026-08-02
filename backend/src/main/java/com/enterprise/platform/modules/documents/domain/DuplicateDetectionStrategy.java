package com.enterprise.platform.modules.documents.domain;

public enum DuplicateDetectionStrategy {
    REJECT_DUPLICATE,
    CREATE_NEW_REFERENCE,
    CREATE_NEW_DOCUMENT,
    ALLOW_DUPLICATE
}
