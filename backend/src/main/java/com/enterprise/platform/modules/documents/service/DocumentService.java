package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DocumentService {

    Document createDocument(String title, UUID folderId, String tenantId, UUID workspaceId, UUID ownerId);

    Document renameDocument(UUID id, String newTitle, String tenantId, UUID userId);

    Document moveDocument(UUID id, UUID targetFolderId, String tenantId, UUID userId);

    void archiveDocument(UUID id, String tenantId, UUID userId);

    Document restoreDocument(UUID id, String tenantId, UUID userId);

    void deleteDocument(UUID id, String tenantId, UUID userId);

    Document updateMetadata(UUID id, Map<String, Object> metadata, String tenantId, UUID userId);

    Document changeOwnership(UUID id, UUID newOwnerId, String tenantId, UUID userId);

    Document assignTags(UUID id, Set<UUID> tagIds, String tenantId, UUID userId);

    Document removeTags(UUID id, Set<UUID> tagIds, String tenantId, UUID userId);

    Document getDocumentOrThrow(UUID id, String tenantId);

    Page<Document> getDocuments(UUID folderId, String tenantId, Pageable pageable);

    Page<Document> searchByMetadata(String key, String value, String tenantId, Pageable pageable);

    com.enterprise.platform.modules.documents.service.dto.LightweightVersionDto createVersion(
            UUID documentId, UUID storageObjectId, UUID userId, String tenantId, 
            com.enterprise.platform.modules.documents.domain.VersionType versionType, String comment);

    com.enterprise.platform.modules.documents.service.dto.LightweightVersionDto getLatestVersion(UUID documentId, String tenantId, UUID userId);

    com.enterprise.platform.modules.documents.service.dto.LightweightVersionDto getVersion(UUID documentId, int versionNumber, String tenantId, UUID userId);

    com.enterprise.platform.modules.documents.service.dto.VersionHistoryDto getVersionHistory(UUID documentId, String tenantId, UUID userId, int page, int size);

    com.enterprise.platform.modules.storage.service.dto.StorageResource downloadVersion(UUID documentId, int versionNumber, String tenantId, UUID userId);
}
