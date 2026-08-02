package com.enterprise.platform.modules.documents.api;

import com.enterprise.platform.modules.authorization.annotation.RequirePermission;
import com.enterprise.platform.modules.authorization.domain.AuthorizationContext;
import com.enterprise.platform.modules.documents.domain.Folder;
import com.enterprise.platform.modules.documents.service.FolderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    @RequirePermission("folders:create")
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody FolderCreateRequest request) {
        AuthorizationContext ctx = getSecurityContext();
        Folder folder = folderService.createFolder(
                request.getName(),
                request.getParentFolderId(),
                ctx.getTenantId(),
                request.getWorkspaceId(),
                ctx.getUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new FolderResponse(folder));
    }

    @PutMapping("/{id}")
    @RequirePermission("folders:update")
    public ResponseEntity<FolderResponse> renameFolder(
            @PathVariable UUID id,
            @RequestParam String name) {
        AuthorizationContext ctx = getSecurityContext();
        Folder folder = folderService.renameFolder(id, name, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new FolderResponse(folder));
    }

    @PostMapping("/{id}/move")
    @RequirePermission("folders:update")
    public ResponseEntity<FolderResponse> moveFolder(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID parentFolderId) {
        AuthorizationContext ctx = getSecurityContext();
        Folder folder = folderService.moveFolder(id, parentFolderId, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new FolderResponse(folder));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("folders:delete")
    public ResponseEntity<Void> deleteFolder(@PathVariable UUID id) {
        AuthorizationContext ctx = getSecurityContext();
        folderService.deleteFolder(id, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @RequirePermission("folders:restore")
    public ResponseEntity<FolderResponse> restoreFolder(@PathVariable UUID id) {
        AuthorizationContext ctx = getSecurityContext();
        Folder folder = folderService.restoreFolder(id, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new FolderResponse(folder));
    }

    @PostMapping("/{id}/archive")
    @RequirePermission("folders:archive")
    public ResponseEntity<Void> archiveFolder(@PathVariable UUID id) {
        AuthorizationContext ctx = getSecurityContext();
        folderService.archiveFolder(id, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/breadcrumbs")
    @RequirePermission("folders:read")
    public ResponseEntity<List<FolderResponse>> getBreadcrumbs(@PathVariable UUID id) {
        AuthorizationContext ctx = getSecurityContext();
        List<Folder> breadcrumbs = folderService.getBreadcrumbs(id, ctx.getTenantId());
        List<FolderResponse> response = breadcrumbs.stream()
                .map(FolderResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tree")
    @RequirePermission("folders:read")
    public ResponseEntity<List<FolderResponse>> getTree(@RequestParam(required = false) UUID parentId) {
        AuthorizationContext ctx = getSecurityContext();
        List<Folder> folders = folderService.getTree(parentId, ctx.getTenantId());
        List<FolderResponse> response = folders.stream()
                .map(FolderResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private AuthorizationContext getSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthorizationContext) {
            return (AuthorizationContext) auth.getPrincipal();
        }
        throw new IllegalStateException("Unauthenticated request");
    }
}
