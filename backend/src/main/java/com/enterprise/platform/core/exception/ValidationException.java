package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends PlatformException {
    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message);
    }
}
