package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends PlatformException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.AUTHENTICATION_REQUIRED, message);
    }
}
