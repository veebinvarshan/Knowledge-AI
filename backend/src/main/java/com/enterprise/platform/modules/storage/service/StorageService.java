package com.enterprise.platform.modules.storage.service;

import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.exception.StorageException;
import com.enterprise.platform.modules.storage.service.dto.*;

import java.io.InputStream;

public interface StorageService {

    StorageObject store(InputStream inputStream, String logicalPath, String mimeType) throws StorageException;

    StorageResource retrieve(String logicalPath) throws StorageException;

    void delete(String logicalPath) throws StorageException;

    boolean exists(String logicalPath);

    StorageObject copy(String sourceLogicalPath, String destLogicalPath) throws StorageException;

    StorageObject move(String sourceLogicalPath, String destLogicalPath) throws StorageException;

    SignedUrl generateSignedUrl(String logicalPath, long expirationSeconds, String httpMethod) throws StorageException;
}
