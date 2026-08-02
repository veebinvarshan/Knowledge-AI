package com.enterprise.platform.modules.documents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "folders", indexes = {
    @Index(name = "idx_folders_tenant_parent", columnList = "tenant_id, parent_folder_id"),
    @Index(name = "idx_folders_materialized_path", columnList = "materialized_path"),
    @Index(name = "idx_folders_tenant", columnList = "tenant_id")
})
public class Folder {

    private static final Pattern INVALID_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "workspace_id")
    private UUID workspaceId;

    @Column(name = "parent_folder_id")
    private UUID parentFolderId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "materialized_path", nullable = false, length = 1000)
    private String materializedPath;

    @Column(nullable = false)
    private boolean archived = false;

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

    public Folder() {}

    public Folder(String name, UUID parentFolderId, String tenantId, UUID workspaceId, UUID userId) {
        validateName(name);
        this.name = name;
        this.parentFolderId = parentFolderId;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.createdBy = userId;
        this.updatedBy = userId;
        this.materializedPath = ""; // Set by service layer during node insertion
    }

    public void rename(String newName, UUID userId) {
        validateName(newName);
        this.name = newName;
        this.updatedBy = userId;
    }

    public void updateParent(UUID newParentId, String parentPath, UUID userId) {
        this.parentFolderId = newParentId;
        this.updatedBy = userId;
        updateMaterializedPath(parentPath);
    }

    public void updateMaterializedPath(String parentPath) {
        if (parentPath == null || parentPath.isEmpty()) {
            this.materializedPath = this.id.toString() + "/";
        } else {
            this.materializedPath = parentPath + this.id.toString() + "/";
        }
    }

    private void validateName(String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be empty");
        }
        if (folderName.length() > 255) {
            throw new IllegalArgumentException("Folder name cannot exceed 255 characters");
        }
        if (INVALID_CHARS.matcher(folderName).find()) {
            throw new IllegalArgumentException("Folder name contains invalid characters: \\ / : * ? \" < > |");
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }

    public UUID getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(UUID parentFolderId) { this.parentFolderId = parentFolderId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMaterializedPath() { return materializedPath; }
    public void setMaterializedPath(String materializedPath) { this.materializedPath = materializedPath; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

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
}
