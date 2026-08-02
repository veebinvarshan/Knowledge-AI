package com.enterprise.platform.modules.documents.upload.exception;

import com.enterprise.platform.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConcurrentFinalizationException extends UploadException {
    public ConcurrentFinalizationException(String message) {
        super(HttpStatus.CONFLICT, ErrorCode.VALIDATION_FAILED, message);
    }
}
