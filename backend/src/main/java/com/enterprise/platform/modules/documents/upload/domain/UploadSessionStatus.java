package com.enterprise.platform.modules.documents.upload.domain;

public enum UploadSessionStatus {
    INITIALIZED,
    UPLOADING,
    COMPLETED,
    FAILED,
    EXPIRED,
    ABORTED
}
