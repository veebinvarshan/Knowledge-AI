package com.enterprise.platform.modules.storage.exception;

import com.enterprise.platform.core.exception.ErrorCode;
import com.enterprise.platform.core.exception.InfrastructureException;
import org.springframework.http.HttpStatus;

public class StorageException extends InfrastructureException {
    
    public StorageException(HttpStatus status, ErrorCode code, String message) {
        super(status, code, message);
    }

    public StorageException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    public StorageException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
