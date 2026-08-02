package com.enterprise.platform.infrastructure.storage;

import com.enterprise.platform.core.config.properties.AzureStorageProperties;
import com.enterprise.platform.modules.storage.exception.StorageException;
import com.enterprise.platform.modules.storage.exception.StorageWriteException;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import com.enterprise.platform.modules.storage.service.dto.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

public class AzureBlobStorageProvider implements StorageProvider {

    private final AzureStorageProperties properties;

    public AzureBlobStorageProvider(AzureStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getProviderId() {
        return "AZURE";
    }

    @Override
    public StorageLocation store(InputStream inputStream, String logicalPath, String mimeType) throws StorageWriteException {
        String objectKey = UUID.randomUUID().toString();
        return new StorageLocation("AZURE", logicalPath, objectKey);
    }

    @Override
    public StorageResource retrieve(String objectKey) {
        throw new UnsupportedOperationException("Azure storage provider retrieve is stubbed");
    }

    @Override
    public void delete(String objectKey) throws StorageException {
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
        String azureUrl = "https://" + properties.container() + ".blob.core.windows.net/" + objectKey;
        return new SignedUrl(azureUrl, Instant.now().plusSeconds(expirationSeconds), "AZURE", httpMethod);
    }

    @Override
    public StorageHealth checkHealth() {
        return new StorageHealth("AZURE", "UP", "Azure Storage Provider stub is healthy");
    }
}
