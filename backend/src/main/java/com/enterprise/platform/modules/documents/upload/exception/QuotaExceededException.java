package com.enterprise.platform.modules.documents.upload.exception;

import com.enterprise.platform.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class QuotaExceededException extends UploadException {
    public QuotaExceededException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message);
    }
}
