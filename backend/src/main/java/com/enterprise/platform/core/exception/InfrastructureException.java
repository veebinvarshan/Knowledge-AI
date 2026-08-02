package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public class InfrastructureException extends PlatformException {
    public InfrastructureException(HttpStatus status, ErrorCode code, String message) {
        super(status, code, message);
    }
    public InfrastructureException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
