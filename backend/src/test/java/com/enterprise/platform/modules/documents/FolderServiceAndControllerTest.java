package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.Folder;
import com.enterprise.platform.modules.documents.repository.FolderRepository;
import com.enterprise.platform.modules.documents.service.FolderService;
import com.enterprise.platform.modules.documents.service.FolderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FolderServiceAndControllerTest {

    private FolderRepository folderRepository;
    private ApplicationEventPublisher eventPublisher;
    private FolderService folderService;

    private String tenantId;
    private UUID userId;
    private UUID workspaceId;

    @BeforeEach
    void setUp() {
        folderRepository = mock(FolderRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        folderService = new FolderServiceImpl(folderRepository, eventPublisher, 5); // Max depth 5 for testing

        tenantId = "acme-corp";
        userId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
    }

    @Test
    void testCreateRootFolderSuccess() {
        // GIVEN
        String folderName = "Documents";
        when(folderRepository.findRootFolderByName(tenantId, folderName)).thenReturn(Optional.empty());
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> {
            Folder f = invocation.getArgument(0);
            if (f.getId() == null) {
                f.setId(UUID.randomUUID());
            }
            return f;
        });

        // WHEN
        Folder created = folderService.createFolder(folderName, null, tenantId, workspaceId, userId);

        // THEN
        assertNotNull(created);
        assertEquals(folderName, created.getName());
        assertNull(created.getParentFolderId());
        assertEquals(created.getId().toString() + "/", created.getMaterializedPath());
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void testCreateFolderWithInvalidNameFails() {
        // GIVEN / WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
            folderService.createFolder("Inval/id", null, tenantId, workspaceId, userId)
        );
        assertThrows(IllegalArgumentException.class, () ->
            folderService.createFolder("   ", null, tenantId, workspaceId, userId)
        );
    }

    @Test
    void testCreateFolderDuplicateSiblingFails() {
        // GIVEN
        String folderName = "Reports";
        UUID parentId = UUID.randomUUID();
        
        Folder existing = new Folder(folderName, parentId, tenantId, workspaceId, userId);
        when(folderRepository.findByTenantIdAndParentFolderIdAndNameAndDeletedAtIsNull(tenantId, parentId, folderName))
                .thenReturn(Optional.of(existing));

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
            folderService.createFolder(folderName, parentId, tenantId, workspaceId, userId)
        );
    }

    @Test
    void testCreateFolderExceedMaxDepthFails() {
        // GIVEN
        UUID parentId = UUID.randomUUID();
        Folder parent = new Folder("DeepChild", parentId, tenantId, workspaceId, userId);
        parent.setId(parentId);
        // Path has 5 segments already (max limit)
        parent.setMaterializedPath("1/2/3/4/5/");
        
        when(folderRepository.findById(parentId)).thenReturn(Optional.of(parent));

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
            folderService.createFolder("NewChild", parentId, tenantId, workspaceId, userId)
        );
    }

    @Test
    void testMoveFolderCircularReferenceFails() {
        // GIVEN
        UUID folderId = UUID.randomUUID();
        Folder folder = new Folder("ParentFolder", null, tenantId, workspaceId, userId);
        folder.setId(folderId);
        folder.setMaterializedPath(folderId.toString() + "/");

        UUID childId = UUID.randomUUID();
        Folder child = new Folder("ChildFolder", folderId, tenantId, workspaceId, userId);
        child.setId(childId);
        child.setMaterializedPath(folderId.toString() + "/" + childId.toString() + "/");

        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
        when(folderRepository.findById(childId)).thenReturn(Optional.of(child));

        // WHEN / THEN (Attempt to move ParentFolder into ChildFolder)
        assertThrows(IllegalArgumentException.class, () ->
            folderService.moveFolder(folderId, childId, tenantId, userId)
        );
    }

    @Test
    void testMoveFolderRecalculatesDescendantPaths() {
        // GIVEN
        UUID folderId = UUID.randomUUID();
        Folder folder = new Folder("Target", null, tenantId, workspaceId, userId);
        folder.setId(folderId);
        folder.setMaterializedPath(folderId.toString() + "/");

        UUID subId = UUID.randomUUID();
        Folder subFolder = new Folder("Sub", folderId, tenantId, workspaceId, userId);
        subFolder.setId(subId);
        subFolder.setMaterializedPath(folderId.toString() + "/" + subId.toString() + "/");

        UUID newParentId = UUID.randomUUID();
        Folder newParent = new Folder("NewParent", null, tenantId, workspaceId, userId);
        newParent.setId(newParentId);
        newParent.setMaterializedPath(newParentId.toString() + "/");

        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
        when(folderRepository.findById(newParentId)).thenReturn(Optional.of(newParent));
        when(folderRepository.findByMaterializedPathStartingWithAndTenantId(folder.getMaterializedPath(), tenantId))
                .thenReturn(List.of(folder, subFolder));

        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Folder moved = folderService.moveFolder(folderId, newParentId, tenantId, userId);

        // THEN
        assertNotNull(moved);
        assertEquals(newParentId, moved.getParentFolderId());
        assertEquals(newParentId.toString() + "/" + folderId.toString() + "/", moved.getMaterializedPath());
        
        // SubFolder path must cascade update
        assertEquals(newParentId.toString() + "/" + folderId.toString() + "/" + subId.toString() + "/", subFolder.getMaterializedPath());
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void testTenantIsolationOnFolderRetrieval() {
        // GIVEN
        UUID folderId = UUID.randomUUID();
        Folder otherFolder = new Folder("Secret", null, "other-tenant", workspaceId, userId);
        otherFolder.setId(folderId);
        
        when(folderRepository.findById(folderId)).thenReturn(Optional.of(otherFolder));

        // WHEN / THEN
        assertThrows(NoSuchElementException.class, () ->
            folderService.renameFolder(folderId, "NewName", "acme-corp", userId)
        );
    }
}
