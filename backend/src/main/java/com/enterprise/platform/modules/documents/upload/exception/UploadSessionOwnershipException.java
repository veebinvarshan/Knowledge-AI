package com.enterprise.platform.modules.documents.upload.exception;

import com.enterprise.platform.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UploadSessionOwnershipException extends UploadException {
    public UploadSessionOwnershipException(String message) {
        super(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, message);
    }
}
