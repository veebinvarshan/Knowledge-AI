package com.enterprise.platform.modules.storage.exception;

import com.enterprise.platform.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class StorageNotFoundException extends StorageException {
    public StorageNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.DOCUMENT_NOT_FOUND, message);
    }
}
