package com.enterprise.platform.modules.documents.exception;

import com.enterprise.platform.core.exception.ConflictException;

public class DuplicateDocumentException extends ConflictException {
    public DuplicateDocumentException(String message) {
        super(message);
    }
}
