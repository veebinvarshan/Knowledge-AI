package com.enterprise.platform.modules.documents.upload.exception;

import com.enterprise.platform.core.exception.ErrorCode;
import com.enterprise.platform.core.exception.InfrastructureException;
import org.springframework.http.HttpStatus;

public class UploadException extends InfrastructureException {
    
    public UploadException(HttpStatus status, ErrorCode code, String message) {
        super(status, code, message);
    }

    public UploadException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
