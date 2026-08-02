package com.enterprise.platform.modules.documents.api;

import com.enterprise.platform.modules.documents.domain.Folder;
import java.time.Instant;
import java.util.UUID;

public class FolderResponse {

    private UUID id;
    private String name;
    private UUID parentFolderId;
    private UUID workspaceId;
    private String materializedPath;
    private boolean archived;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public FolderResponse() {}

    public FolderResponse(Folder folder) {
        this.id = folder.getId();
        this.name = folder.getName();
        this.parentFolderId = folder.getParentFolderId();
        this.workspaceId = folder.getWorkspaceId();
        this.materializedPath = folder.getMaterializedPath();
        this.archived = folder.isArchived();
        this.createdBy = folder.getCreatedBy();
        this.createdAt = folder.getCreatedAt();
        this.updatedAt = folder.getUpdatedAt();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(UUID parentFolderId) { this.parentFolderId = parentFolderId; }

    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }

    public String getMaterializedPath() { return materializedPath; }
    public void setMaterializedPath(String materializedPath) { this.materializedPath = materializedPath; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
