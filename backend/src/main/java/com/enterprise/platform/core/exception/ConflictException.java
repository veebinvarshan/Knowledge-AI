package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends PlatformException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
