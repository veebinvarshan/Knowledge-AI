package com.enterprise.platform.modules.documents.api;

import com.enterprise.platform.modules.documents.domain.Document;
import com.enterprise.platform.modules.documents.domain.LifecycleStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class DocumentResponse {

    private UUID id;
    private String title;
    private String slug;
    private UUID folderId;
    private UUID workspaceId;
    private UUID ownerId;
    private UUID currentVersionId;
    private LifecycleStatus status;
    private Map<String, Object> structuredMetadata;
    private Instant createdAt;
    private Instant updatedAt;

    public DocumentResponse() {}

    public DocumentResponse(Document doc) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.slug = doc.getSlug();
        this.folderId = doc.getFolderId();
        this.workspaceId = doc.getWorkspaceId();
        this.ownerId = doc.getOwnerId();
        this.currentVersionId = doc.getCurrentVersionId();
        this.status = doc.getStatus();
        this.structuredMetadata = doc.getStructuredMetadata();
        this.createdAt = doc.getCreatedAt();
        this.updatedAt = doc.getUpdatedAt();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public UUID getFolderId() { return folderId; }
    public void setFolderId(UUID folderId) { this.folderId = folderId; }

    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public UUID getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(UUID currentVersionId) { this.currentVersionId = currentVersionId; }

    public LifecycleStatus getStatus() { return status; }
    public void setStatus(LifecycleStatus status) { this.status = status; }

    public Map<String, Object> getStructuredMetadata() { return structuredMetadata; }
    public void setStructuredMetadata(Map<String, Object> structuredMetadata) { this.structuredMetadata = structuredMetadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
