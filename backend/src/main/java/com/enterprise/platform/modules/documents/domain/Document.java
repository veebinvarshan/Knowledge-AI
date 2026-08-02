package com.enterprise.platform.modules.documents.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_documents_tenant_folder", columnList = "tenant_id, folder_id"),
    @Index(name = "idx_documents_tenant_slug", columnList = "tenant_id, slug"),
    @Index(name = "idx_documents_tenant", columnList = "tenant_id")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "workspace_id")
    private UUID workspaceId;

    @Column(name = "folder_id")
    private UUID folderId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String slug;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "current_version_id")
    private UUID currentVersionId;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DocumentVersion> versions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LifecycleStatus status = LifecycleStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_metadata", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> structuredMetadata = new HashMap<>();

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Document() {}

    public Document(String title, String slug, UUID folderId, String tenantId, UUID workspaceId, UUID ownerId) {
        validateTitle(title);
        this.title = title;
        this.slug = slug;
        this.folderId = folderId;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.ownerId = ownerId;
        this.createdBy = ownerId;
        this.updatedBy = ownerId;
        this.status = LifecycleStatus.DRAFT;
    }

    public void rename(String newTitle, String newSlug, UUID userId) {
        validateDeleted();
        validateTitle(newTitle);
        this.title = newTitle;
        this.slug = newSlug;
        this.updatedBy = userId;
    }

    public void move(UUID targetFolderId, UUID userId) {
        validateDeleted();
        this.folderId = targetFolderId;
        this.updatedBy = userId;
    }

    public void archive(UUID userId) {
        validateDeleted();
        if (this.status != LifecycleStatus.READY) {
            throw new IllegalStateException("Only documents in READY state can be archived");
        }
        this.status = LifecycleStatus.ARCHIVED;
        this.updatedBy = userId;
    }

    public void restore(UUID userId) {
        if (this.deletedAt == null && this.status != LifecycleStatus.ARCHIVED) {
            throw new IllegalStateException("Document is not archived or deleted");
        }
        if (this.status == LifecycleStatus.ARCHIVED) {
            this.status = LifecycleStatus.READY;
        }
        this.deletedAt = null;
        this.updatedBy = userId;
    }

    public void softDelete(UUID userId) {
        validateDeleted();
        this.deletedAt = Instant.now();
        this.updatedBy = userId;
    }

    public void updateMetadata(Map<String, Object> metadata, UUID userId) {
        validateDeleted();
        validateMetadata(metadata);
        this.structuredMetadata = new HashMap<>(metadata);
        this.updatedBy = userId;
    }

    public void changeOwner(UUID newOwnerId, UUID userId) {
        validateDeleted();
        if (newOwnerId == null) {
            throw new IllegalArgumentException("New owner ID cannot be null");
        }
        this.ownerId = newOwnerId;
        this.updatedBy = userId;
    }

    private void validateTitle(String docTitle) {
        if (docTitle == null || docTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Document title cannot be empty");
        }
        if (docTitle.length() > 255) {
            throw new IllegalArgumentException("Document title cannot exceed 255 characters");
        }
    }

    private void validateDeleted() {
        if (this.deletedAt != null) {
            throw new IllegalStateException("Cannot modify a soft-deleted document");
        }
    }

    private void validateMetadata(Map<String, Object> metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata map cannot be null");
        }
        if (metadata.size() > 100) {
            throw new IllegalArgumentException("Structured metadata limit exceeded: maximum 100 items");
        }
        for (String key : metadata.keySet()) {
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("Metadata keys cannot be empty");
            }
            if (key.contains(" ") || !key.matches("^[a-zA-Z0-9_\\-:]+$")) {
                throw new IllegalArgumentException("Metadata keys cannot contain spaces or special characters");
            }
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }

    public UUID getFolderId() { return folderId; }
    public void setFolderId(UUID folderId) { this.folderId = folderId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public UUID getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(UUID currentVersionId) { this.currentVersionId = currentVersionId; }

    public LifecycleStatus getStatus() { return status; }
    public void setStatus(LifecycleStatus status) { this.status = status; }

    public Map<String, Object> getStructuredMetadata() { return structuredMetadata; }
    public void setStructuredMetadata(Map<String, Object> structuredMetadata) { this.structuredMetadata = structuredMetadata; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public List<DocumentVersion> getVersions() { return versions; }

    public DocumentVersion addVersion(UUID storageObjectId, String checksum, String checksumAlgorithm, 
                                       long sizeBytes, String mimeType, UUID createdBy, VersionType versionType, String comment) {
        validateDeleted();

        // 1. Mark existing ACTIVE version as SUPERSEDED
        versions.stream()
                .filter(v -> v.getStatus() == VersionStatus.ACTIVE)
                .forEach(v -> v.setStatus(VersionStatus.SUPERSEDED));

        int nextNumber = versions.isEmpty() ? 1 : versions.stream().mapToInt(DocumentVersion::getVersionNumber).max().orElse(0) + 1;

        // 2. Build new immutable version
        DocumentVersion nextVersion = new DocumentVersion(this, nextNumber, storageObjectId, checksum, checksumAlgorithm, sizeBytes, mimeType, createdBy, versionType, comment);
        versions.add(nextVersion);

        // 3. Update current version reference
        this.currentVersionId = nextVersion.getId();

        return nextVersion;
    }
}
