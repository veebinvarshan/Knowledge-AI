package com.enterprise.platform.modules.documents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_versions", uniqueConstraints = {
    @UniqueConstraint(name = "uq_document_versions_num", columnNames = {"document_id", "version_number"})
}, indexes = {
    @Index(name = "idx_document_versions_doc", columnList = "document_id")
})
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "storage_object_id", nullable = false)
    private UUID storageObjectId;

    @Column(nullable = false, length = 255)
    private String checksum;

    @Column(name = "checksum_algorithm", nullable = false, length = 50)
    private String checksumAlgorithm;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "mime_type", nullable = false, length = 255)
    private String mimeType;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "version_type", nullable = false, length = 50)
    private VersionType versionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VersionStatus status;

    @Column(length = 1024)
    private String comment;

    // Optional metadata extension fields (Comparison / Retention Readiness)
    @Column(name = "original_file_name", length = 1024)
    private String originalFileName;

    @Column(length = 50)
    private String extension;

    @Column(name = "media_category", length = 100)
    private String mediaCategory;

    @Column(name = "retention_until")
    private Instant retentionUntil;

    @Column(name = "legal_hold", nullable = false)
    private boolean legalHold = false;

    public DocumentVersion() {}

    public DocumentVersion(Document document, Integer versionNumber, UUID storageObjectId, String checksum, 
                           String checksumAlgorithm, Long sizeBytes, String mimeType, UUID createdBy, 
                           VersionType versionType, String comment) {
        this.document = document;
        this.versionNumber = versionNumber;
        this.storageObjectId = storageObjectId;
        this.checksum = checksum;
        this.checksumAlgorithm = checksumAlgorithm;
        this.sizeBytes = sizeBytes;
        this.mimeType = mimeType;
        this.createdBy = createdBy;
        this.versionType = versionType;
        this.status = VersionStatus.ACTIVE;
        this.comment = comment;
        this.createdAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Document getDocument() { return document; }
    public Integer getVersionNumber() { return versionNumber; }
    public UUID getStorageObjectId() { return storageObjectId; }
    public String getChecksum() { return checksum; }
    public String getChecksumAlgorithm() { return checksumAlgorithm; }
    public Long getSizeBytes() { return sizeBytes; }
    public String getMimeType() { return mimeType; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public VersionType getVersionType() { return versionType; }
    public VersionStatus getStatus() { return status; }
    public String getComment() { return comment; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public String getMediaCategory() { return mediaCategory; }
    public void setMediaCategory(String mediaCategory) { this.mediaCategory = mediaCategory; }

    public Instant getRetentionUntil() { return retentionUntil; }
    public void setRetentionUntil(Instant retentionUntil) { this.retentionUntil = retentionUntil; }

    public boolean isLegalHold() { return legalHold; }
    public void setLegalHold(boolean legalHold) { this.legalHold = legalHold; }

    // Mutator restricted strictly to allowed status changes
    public void setStatus(VersionStatus status) {
        if (this.status == VersionStatus.ARCHIVED && status == VersionStatus.SUPERSEDED) {
            throw new IllegalStateException("Cannot change version state from ARCHIVED to SUPERSEDED");
        }
        this.status = status;
    }
}
