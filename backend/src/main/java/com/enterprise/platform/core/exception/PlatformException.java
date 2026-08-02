package com.enterprise.platform.core.exception;

import org.springframework.http.HttpStatus;

public abstract class PlatformException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;

    protected PlatformException(HttpStatus httpStatus, ErrorCode errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    protected PlatformException(HttpStatus httpStatus, ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
