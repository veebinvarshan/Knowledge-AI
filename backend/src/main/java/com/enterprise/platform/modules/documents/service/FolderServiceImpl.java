package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Folder;
import com.enterprise.platform.modules.documents.domain.FolderEvents.*;
import com.enterprise.platform.modules.documents.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final int maxDepth;

    public FolderServiceImpl(
            FolderRepository folderRepository,
            ApplicationEventPublisher eventPublisher,
            @Value("${platform.folders.max-depth:10}") int maxDepth) {
        this.folderRepository = folderRepository;
        this.eventPublisher = eventPublisher;
        this.maxDepth = maxDepth;
    }

    @Override
    public Folder createFolder(String name, UUID parentId, String tenantId, UUID workspaceId, UUID userId) {
        // 1. Sibling duplicate check
        validateSiblingDuplicate(name, parentId, tenantId, null);

        // 2. Instantiate folder
        Folder folder = new Folder(name, parentId, tenantId, workspaceId, userId);
        
        // 3. Temporarily save to generate ID for materialized path calculation
        folder = folderRepository.save(folder);

        // 4. Resolve path and calculate materialized path
        String parentPath = "";
        if (parentId != null) {
            Folder parent = getFolderOrThrow(parentId, tenantId);
            parentPath = parent.getMaterializedPath();
            
            // Check depth validation
            int currentDepth = parentPath.split("/").length;
            if (currentDepth >= maxDepth) {
                throw new IllegalArgumentException("Folder depth limit exceeded. Maximum depth is " + maxDepth);
            }
        }
        
        folder.updateMaterializedPath(parentPath);
        folder = folderRepository.save(folder);

        // 5. Publish domain event
        eventPublisher.publishEvent(new FolderCreatedEvent(folder.getId(), folder.getName(), tenantId, userId));
        return folder;
    }

    @Override
    public Folder renameFolder(UUID id, String newName, String tenantId, UUID userId) {
        Folder folder = getFolderOrThrow(id, tenantId);
        
        // Sibling duplicate check excluding itself
        validateSiblingDuplicate(newName, folder.getParentFolderId(), tenantId, id);

        String oldName = folder.getName();
        folder.rename(newName, userId);
        folder = folderRepository.save(folder);

        eventPublisher.publishEvent(new FolderRenamedEvent(id, oldName, newName, tenantId, userId));
        return folder;
    }

    @Override
    public Folder moveFolder(UUID id, UUID newParentId, String tenantId, UUID userId) {
        Folder folder = getFolderOrThrow(id, tenantId);
        
        if (Objects.equals(folder.getParentFolderId(), newParentId)) {
            return folder;
        }

        // 1. Check duplicate inside new parent
        validateSiblingDuplicate(folder.getName(), newParentId, tenantId, id);

        // 2. Load sub-hierarchy of target folder
        String oldPathPrefix = folder.getMaterializedPath();
        List<Folder> descendants = folderRepository.findByMaterializedPathStartingWithAndTenantId(oldPathPrefix, tenantId);

        String newParentPath = "";
        if (newParentId != null) {
            Folder newParent = getFolderOrThrow(newParentId, tenantId);
            newParentPath = newParent.getMaterializedPath();

            // Circular reference check
            if (newParent.getMaterializedPath().startsWith(oldPathPrefix)) {
                throw new IllegalArgumentException("Cannot move a folder into its own subfolder");
            }

            // Calculate total depth if move completes
            int newParentDepth = newParentPath.split("/").length;
            int maxSubtreeDepth = 0;
            for (Folder desc : descendants) {
                String subPath = desc.getMaterializedPath().substring(oldPathPrefix.length());
                int subDepth = subPath.isEmpty() ? 0 : subPath.split("/").length;
                maxSubtreeDepth = Math.max(maxSubtreeDepth, subDepth);
            }

            if (newParentDepth + 1 + maxSubtreeDepth > maxDepth) {
                throw new IllegalArgumentException("Move operation fails: exceeds maximum allowed folder depth of " + maxDepth);
            }
        }

        UUID oldParentId = folder.getParentFolderId();
        
        // 3. Relocate target folder
        folder.updateParent(newParentId, newParentPath, userId);
        folder = folderRepository.save(folder);

        // 4. Update descendants paths recursively
        String newPathPrefix = folder.getMaterializedPath();
        for (Folder desc : descendants) {
            if (!desc.getId().equals(id)) {
                String relativePath = desc.getMaterializedPath().substring(oldPathPrefix.length());
                desc.setMaterializedPath(newPathPrefix + relativePath);
                desc.setUpdatedBy(userId);
                folderRepository.save(desc);
            }
        }

        eventPublisher.publishEvent(new FolderMovedEvent(id, oldParentId, newParentId, tenantId, userId));
        return folder;
    }

    @Override
    public void deleteFolder(UUID id, String tenantId, UUID userId) {
        Folder folder = getFolderOrThrow(id, tenantId);
        Instant now = Instant.now();

        // Soft-delete current and all descendants
        folder.setDeletedAt(now);
        folder.setUpdatedBy(userId);
        folderRepository.save(folder);

        List<Folder> descendants = folderRepository.findByMaterializedPathStartingWithAndTenantId(folder.getMaterializedPath(), tenantId);
        for (Folder desc : descendants) {
            desc.setDeletedAt(now);
            desc.setUpdatedBy(userId);
            folderRepository.save(desc);
        }

        eventPublisher.publishEvent(new FolderDeletedEvent(id, tenantId, userId));
    }

    @Override
    public Folder restoreFolder(UUID id, String tenantId, UUID userId) {
        // Bypass active soft-delete filter by finding using JpaRepository directly, then checking tenant bounds
        Folder folder = folderRepository.findById(id)
                .filter(f -> f.getTenantId().equals(tenantId))
                .orElseThrow(() -> new NoSuchElementException("Folder not found"));

        folder.setDeletedAt(null);
        folder.setUpdatedBy(userId);
        folder = folderRepository.save(folder);

        List<Folder> descendants = folderRepository.findByMaterializedPathStartingWithAndTenantId(folder.getMaterializedPath(), tenantId);
        for (Folder desc : descendants) {
            desc.setDeletedAt(null);
            desc.setUpdatedBy(userId);
            folderRepository.save(desc);
        }

        eventPublisher.publishEvent(new FolderRestoredEvent(id, tenantId, userId));
        return folder;
    }

    @Override
    public void archiveFolder(UUID id, String tenantId, UUID userId) {
        Folder folder = getFolderOrThrow(id, tenantId);
        
        folder.setArchived(true);
        folder.setUpdatedBy(userId);
        folderRepository.save(folder);

        List<Folder> descendants = folderRepository.findByMaterializedPathStartingWithAndTenantId(folder.getMaterializedPath(), tenantId);
        for (Folder desc : descendants) {
            desc.setArchived(true);
            desc.setUpdatedBy(userId);
            folderRepository.save(desc);
        }

        eventPublisher.publishEvent(new FolderArchivedEvent(id, tenantId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Folder> getBreadcrumbs(UUID id, String tenantId) {
        Folder folder = getFolderOrThrow(id, tenantId);
        String path = folder.getMaterializedPath();
        String[] ids = path.split("/");
        
        List<UUID> uuids = Arrays.stream(ids)
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<Folder> breadcrumbs = folderRepository.findAllById(uuids);
        
        // Ensure breadcrumbs are sorted in structural sequence
        Map<UUID, Folder> folderMap = breadcrumbs.stream()
                .collect(Collectors.toMap(Folder::getId, f -> f));
        
        return uuids.stream()
                .map(folderMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Folder> getTree(UUID parentId, String tenantId) {
        if (parentId == null) {
            return folderRepository.findRootFolders(tenantId);
        }
        return folderRepository.findByTenantIdAndParentFolderIdAndDeletedAtIsNull(tenantId, parentId);
    }

    private Folder getFolderOrThrow(UUID id, String tenantId) {
        return folderRepository.findById(id)
                .filter(f -> f.getTenantId().equals(tenantId) && f.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Folder not found"));
    }

    private void validateSiblingDuplicate(String name, UUID parentId, String tenantId, UUID selfId) {
        boolean exists;
        if (parentId == null) {
            if (selfId == null) {
                exists = folderRepository.findRootFolderByName(tenantId, name).isPresent();
            } else {
                exists = folderRepository.existsRootFolderByNameAndIdNot(tenantId, name, selfId);
            }
        } else {
            if (selfId == null) {
                exists = folderRepository.findByTenantIdAndParentFolderIdAndNameAndDeletedAtIsNull(tenantId, parentId, name).isPresent();
            } else {
                exists = folderRepository.existsByTenantIdAndParentFolderIdAndNameAndDeletedAtIsNullAndIdNot(tenantId, parentId, name, selfId);
            }
        }

        if (exists) {
            throw new IllegalArgumentException("A folder named '" + name + "' already exists in this directory");
        }
    }
}
