package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends PlatformException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message);
    }
}
