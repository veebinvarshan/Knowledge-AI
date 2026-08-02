package com.enterprise.platform.modules.documents.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class DocumentCreateRequest {

    @NotBlank(message = "Document title cannot be blank")
    @Size(max = 255, message = "Document title cannot exceed 255 characters")
    private String title;

    private UUID folderId;
    private UUID workspaceId;

    public DocumentCreateRequest() {}

    public DocumentCreateRequest(String title, UUID folderId, UUID workspaceId) {
        this.title = title;
        this.folderId = folderId;
        this.workspaceId = workspaceId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public UUID getFolderId() { return folderId; }
    public void setFolderId(UUID folderId) { this.folderId = folderId; }

    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }
}
