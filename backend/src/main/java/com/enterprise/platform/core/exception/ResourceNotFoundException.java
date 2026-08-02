package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends PlatformException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.DOCUMENT_NOT_FOUND, message);
    }
}
