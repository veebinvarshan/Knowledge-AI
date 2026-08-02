package com.enterprise.platform.modules.documents.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class FolderCreateRequest {

    @NotBlank(message = "Folder name cannot be blank")
    @Size(max = 255, message = "Folder name cannot exceed 255 characters")
    private String name;

    private UUID parentFolderId;
    private UUID workspaceId;

    public FolderCreateRequest() {}

    public FolderCreateRequest(String name, UUID parentFolderId, UUID workspaceId) {
        this.name = name;
        this.parentFolderId = parentFolderId;
        this.workspaceId = workspaceId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(UUID parentFolderId) { this.parentFolderId = parentFolderId; }

    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
}
