package com.enterprise.platform.infrastructure.storage;

import com.enterprise.platform.core.config.properties.S3StorageProperties;
import com.enterprise.platform.modules.storage.exception.StorageException;
import com.enterprise.platform.modules.storage.exception.StorageWriteException;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import com.enterprise.platform.modules.storage.service.dto.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

public class S3StorageProvider implements StorageProvider {

    private final S3StorageProperties properties;

    public S3StorageProvider(S3StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getProviderId() {
        return "S3";
    }

    @Override
    public StorageLocation store(InputStream inputStream, String logicalPath, String mimeType) throws StorageWriteException {
        String objectKey = UUID.randomUUID().toString();
        return new StorageLocation("S3", logicalPath, objectKey);
    }

    @Override
    public StorageResource retrieve(String objectKey) {
        throw new UnsupportedOperationException("S3 storage provider retrieve is stubbed");
    }

    @Override
    public void delete(String objectKey) throws StorageException {
        // Stub log
    }

    @Override
    public boolean exists(String logicalPath) {
        return false;
    }

    @Override
    public void copy(String sourceObjectKey, String destLogicalPath) throws StorageException {
    }

    @Override
    public void move(String sourceObjectKey, String destLogicalPath) throws StorageException {
    }

    @Override
    public StorageMetadata readMetadata(String objectKey) {
        return new StorageMetadata(0, "application/octet-stream", "dummy-checksum", "SHA256");
    }

    @Override
    public SignedUrl generateSignedUrl(String objectKey, long expirationSeconds, String httpMethod) {
        String s3Url = "https://" + properties.bucket() + ".s3.amazonaws.com/" + objectKey;
        return new SignedUrl(s3Url, Instant.now().plusSeconds(expirationSeconds), "S3", httpMethod);
    }

    @Override
    public StorageHealth checkHealth() {
        return new StorageHealth("S3", "UP", "S3 Storage Provider stub is healthy");
    }
}
