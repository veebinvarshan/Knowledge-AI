package com.enterprise.platform.modules.storage.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "storage_objects", indexes = {
    @Index(name = "idx_storage_objects_logical_path", columnList = "logical_path"),
    @Index(name = "idx_storage_objects_provider_key", columnList = "provider_id, provider_object_key")
})
public class StorageObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "logical_path", nullable = false, unique = true, length = 1024)
    private String logicalPath;

    @Column(name = "provider_object_key", nullable = false, length = 1024)
    private String providerObjectKey;

    @Column(name = "provider_id", nullable = false, length = 64)
    private String providerId;

    @Column(nullable = false, length = 255)
    private String checksum;

    @Column(name = "checksum_algorithm", nullable = false, length = 50)
    private String checksumAlgorithm;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "mime_type", nullable = false, length = 255)
    private String mimeType;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public StorageObject() {}

    public StorageObject(String logicalPath, String providerObjectKey, String providerId, 
                         String checksum, String checksumAlgorithm, Long sizeBytes, String mimeType) {
        this.logicalPath = logicalPath;
        this.providerObjectKey = providerObjectKey;
        this.providerId = providerId;
        this.checksum = checksum;
        this.checksumAlgorithm = checksumAlgorithm;
        this.sizeBytes = sizeBytes;
        this.mimeType = mimeType;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLogicalPath() { return logicalPath; }
    public void setLogicalPath(String logicalPath) { this.logicalPath = logicalPath; }

    public String getProviderObjectKey() { return providerObjectKey; }
    public void setProviderObjectKey(String providerObjectKey) { this.providerObjectKey = providerObjectKey; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getChecksumAlgorithm() { return checksumAlgorithm; }
    public void setChecksumAlgorithm(String checksumAlgorithm) { this.checksumAlgorithm = checksumAlgorithm; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
