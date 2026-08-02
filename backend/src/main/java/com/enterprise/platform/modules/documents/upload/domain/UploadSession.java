package com.enterprise.platform.modules.documents.upload.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "upload_sessions", indexes = {
    @Index(name = "idx_upload_sessions_tenant", columnList = "tenant_id"),
    @Index(name = "idx_upload_sessions_status_expiry", columnList = "status, expires_at")
})
public class UploadSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "file_name", nullable = false, length = 1024)
    private String fileName;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", nullable = false, length = 255)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UploadSessionStatus status;

    @Column(name = "chunks_total", nullable = false)
    private Integer chunksTotal;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UploadChunk> chunks = new ArrayList<>();

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

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

    public UploadSession() {}

    public UploadSession(String tenantId, UUID userId, String fileName, Long fileSizeBytes, String mimeType, Integer chunksTotal, Instant expiresAt) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.chunksTotal = chunksTotal;
        this.status = UploadSessionStatus.INITIALIZED;
        this.expiresAt = expiresAt;
    }

    public void addChunk(int chunkNumber, long sizeBytes, String checksum, String checksumAlgorithm) {
        if (status != UploadSessionStatus.INITIALIZED && status != UploadSessionStatus.UPLOADING) {
            throw new IllegalStateException("Cannot add chunk to upload session in status: " + status);
        }
        if (chunkNumber < 1 || chunkNumber > chunksTotal) {
            throw new IllegalArgumentException("Invalid chunk number: " + chunkNumber);
        }
        boolean alreadyUploaded = chunks.stream().anyMatch(c -> c.getChunkNumber() == chunkNumber);
        if (alreadyUploaded) {
            throw new IllegalStateException("Duplicate chunk upload: " + chunkNumber);
        }

        UploadChunk chunk = new UploadChunk(this, chunkNumber, sizeBytes, checksum, checksumAlgorithm);
        chunks.add(chunk);

        if (status == UploadSessionStatus.INITIALIZED) {
            status = UploadSessionStatus.UPLOADING;
        }
    }

    public boolean isComplete() {
        return chunks.size() == chunksTotal;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public UploadSessionStatus getStatus() { return status; }
    public void setStatus(UploadSessionStatus status) { this.status = status; }

    public Integer getChunksTotal() { return chunksTotal; }
    public void setChunksTotal(Integer chunksTotal) { this.chunksTotal = chunksTotal; }

    public List<UploadChunk> getChunks() { return chunks; }
    public void setChunks(List<UploadChunk> chunks) { this.chunks = chunks; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
