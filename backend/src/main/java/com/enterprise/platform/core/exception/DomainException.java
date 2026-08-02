package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public class DomainException extends PlatformException {
    public DomainException(HttpStatus status, ErrorCode code, String message) {
        super(status, code, message);
    }
    public DomainException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
