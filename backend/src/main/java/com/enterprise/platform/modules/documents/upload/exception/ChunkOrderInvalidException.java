package com.enterprise.platform.modules.documents.upload.exception;

import com.enterprise.platform.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ChunkOrderInvalidException extends UploadException {
    public ChunkOrderInvalidException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message);
    }
}
