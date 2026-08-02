package com.enterprise.platform.infrastructure.storage;

import com.enterprise.platform.core.config.properties.LocalStorageProperties;
import com.enterprise.platform.modules.storage.exception.StorageException;
import com.enterprise.platform.modules.storage.exception.StorageNotFoundException;
import com.enterprise.platform.modules.storage.exception.StorageReadException;
import com.enterprise.platform.modules.storage.exception.StorageWriteException;
import com.enterprise.platform.modules.storage.service.StorageProvider;
import com.enterprise.platform.modules.storage.service.dto.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

public class LocalStorageProvider implements StorageProvider {

    private final LocalStorageProperties properties;
    private final Path rootPath;

    public LocalStorageProvider(LocalStorageProperties properties) {
        this.properties = properties;
        this.rootPath = Paths.get(properties.rootDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize local storage root directory: " + properties.rootDirectory(), e);
        }
    }

    @Override
    public String getProviderId() {
        return "LOCAL";
    }

    @Override
    public StorageLocation store(InputStream inputStream, String logicalPath, String mimeType) throws StorageWriteException {
        validateLogicalPath(logicalPath);
        
        String extension = getExtension(mimeType);
        String objectKey = UUID.randomUUID().toString() + extension;
        Path targetPath = rootPath.resolve(objectKey).toAbsolutePath().normalize();

        // Security check
        if (!targetPath.startsWith(rootPath)) {
            throw new SecurityException("Access outside configured storage root is denied: " + logicalPath);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(inputStream, digest);
                 OutputStream out = Files.newOutputStream(targetPath)) {
                
                byte[] buffer = new byte[8192];
                int read;
                while ((read = dis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }

            return new StorageLocation("LOCAL", logicalPath, objectKey);
        } catch (Exception e) {
            throw new StorageWriteException("Failed to write file physically on local storage: " + logicalPath, e);
        }
    }

    @Override
    public StorageResource retrieve(String objectKey) throws StorageReadException {
        Path targetPath = rootPath.resolve(objectKey).toAbsolutePath().normalize();
        if (!targetPath.startsWith(rootPath) || !Files.exists(targetPath)) {
            throw new StorageNotFoundException("File not found in local storage: " + objectKey);
        }

        try {
            StorageMetadata metadata = readMetadata(objectKey);
            InputStream in = Files.newInputStream(targetPath);
            return new StorageResource(in, metadata);
        } catch (Exception e) {
            throw new StorageReadException("Failed to retrieve file from local storage: " + objectKey, e);
        }
    }

    @Override
    public void delete(String objectKey) throws StorageException {
        Path targetPath = rootPath.resolve(objectKey).toAbsolutePath().normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new SecurityException("Forbidden access path: " + objectKey);
        }
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new StorageException("Failed to delete physical file in local storage: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String logicalPath) {
        // Since LocalStorageProvider generates keys dynamically, logical path checks require service aggregate lookups.
        // But for direct checks on configured logical keys (if needed):
        return false;
    }

    @Override
    public void copy(String sourceObjectKey, String destLogicalPath) throws StorageException {
        Path sourcePath = rootPath.resolve(sourceObjectKey).toAbsolutePath().normalize();
        if (!sourcePath.startsWith(rootPath) || !Files.exists(sourcePath)) {
            throw new StorageNotFoundException("Source file not found: " + sourceObjectKey);
        }

        // Logical path mapping metadata will copy via StorageService logic
    }

    @Override
    public void move(String sourceObjectKey, String destLogicalPath) throws StorageException {
        Path sourcePath = rootPath.resolve(sourceObjectKey).toAbsolutePath().normalize();
        if (!sourcePath.startsWith(rootPath) || !Files.exists(sourcePath)) {
            throw new StorageNotFoundException("Source file not found: " + sourceObjectKey);
        }
    }

    @Override
    public StorageMetadata readMetadata(String objectKey) throws StorageReadException {
        Path targetPath = rootPath.resolve(objectKey).toAbsolutePath().normalize();
        if (!targetPath.startsWith(rootPath) || !Files.exists(targetPath)) {
            throw new StorageNotFoundException("File not found: " + objectKey);
        }

        try {
            long size = Files.size(targetPath);
            String mime = Files.probeContentType(targetPath);
            if (mime == null) {
                mime = "application/octet-stream";
            }

            // Calculate current file checksum
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(targetPath)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            String checksum = bytesToHex(digest.digest());

            return new StorageMetadata(size, mime, checksum, "SHA256");
        } catch (Exception e) {
            throw new StorageReadException("Failed to read metadata: " + objectKey, e);
        }
    }

    @Override
    public SignedUrl generateSignedUrl(String objectKey, long expirationSeconds, String httpMethod) {
        // Expose a local mock URL for signed operations
        String mockUrl = "http://localhost:8080/api/v1/storage/local/" + objectKey;
        return new SignedUrl(mockUrl, Instant.now().plusSeconds(expirationSeconds), "LOCAL", httpMethod);
    }

    @Override
    public StorageHealth checkHealth() {
        boolean writable = Files.isWritable(rootPath);
        return new StorageHealth("LOCAL", writable ? "UP" : "DOWN", writable ? "Local storage is active and writable" : "Local root directory is not writable");
    }

    private void validateLogicalPath(String logicalPath) {
        if (logicalPath == null || logicalPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Logical path cannot be blank");
        }
        if (logicalPath.contains("..") || logicalPath.contains("\\")) {
            throw new IllegalArgumentException("Invalid logical path character sequence: path traversal blocked");
        }
    }

    private String getExtension(String mimeType) {
        if (mimeType == null) {
            return "";
        }
        return switch (mimeType.toLowerCase()) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "text/plain" -> ".txt";
            case "application/json" -> ".json";
            default -> "";
        };
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
