package com.enterprise.platform.modules.storage.service;

import com.enterprise.platform.modules.storage.exception.StorageException;
import com.enterprise.platform.modules.storage.exception.StorageReadException;
import com.enterprise.platform.modules.storage.exception.StorageWriteException;
import com.enterprise.platform.modules.storage.service.dto.*;

import java.io.InputStream;

public interface StorageProvider {

    String getProviderId();

    StorageLocation store(InputStream inputStream, String logicalPath, String mimeType) throws StorageWriteException;

    StorageResource retrieve(String objectKey) throws StorageReadException;

    void delete(String objectKey) throws StorageException;

    boolean exists(String logicalPath);

    void copy(String sourceObjectKey, String destLogicalPath) throws StorageException;

    void move(String sourceObjectKey, String destLogicalPath) throws StorageException;

    StorageMetadata readMetadata(String objectKey) throws StorageReadException;

    SignedUrl generateSignedUrl(String objectKey, long expirationSeconds, String httpMethod);

    StorageHealth checkHealth();
}
