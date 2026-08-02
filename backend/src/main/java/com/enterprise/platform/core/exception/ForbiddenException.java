package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends PlatformException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, message);
    }
}
