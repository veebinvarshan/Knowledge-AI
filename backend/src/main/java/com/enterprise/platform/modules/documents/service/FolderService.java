package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Folder;
import java.util.List;
import java.util.UUID;

public interface FolderService {

    Folder createFolder(String name, UUID parentId, String tenantId, UUID workspaceId, UUID userId);

    Folder renameFolder(UUID id, String newName, String tenantId, UUID userId);

    Folder moveFolder(UUID id, UUID newParentId, String tenantId, UUID userId);

    void deleteFolder(UUID id, String tenantId, UUID userId);

    Folder restoreFolder(UUID id, String tenantId, UUID userId);

    void archiveFolder(UUID id, String tenantId, UUID userId);

    List<Folder> getBreadcrumbs(UUID id, String tenantId);

    List<Folder> getTree(UUID parentId, String tenantId);
}
