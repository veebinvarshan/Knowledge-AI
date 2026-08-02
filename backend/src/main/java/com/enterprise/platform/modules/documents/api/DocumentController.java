package com.enterprise.platform.modules.documents.api;

import com.enterprise.platform.modules.authorization.annotation.RequirePermission;
import com.enterprise.platform.modules.authorization.domain.AuthorizationContext;
import com.enterprise.platform.modules.documents.domain.Document;
import com.enterprise.platform.modules.documents.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.enterprise.platform.modules.documents.service.dto.LightweightVersionDto;
import com.enterprise.platform.modules.documents.service.dto.VersionHistoryDto;
import com.enterprise.platform.modules.storage.service.dto.StorageResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @RequirePermission("documents:create")
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentCreateRequest request) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.createDocument(
                request.getTitle(),
                request.getFolderId(),
                ctx.getTenantId(),
                request.getWorkspaceId(),
                ctx.getUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentResponse(doc));
    }

    @PutMapping("/{id}")
    @RequirePermission("documents:update")
    public ResponseEntity<DocumentResponse> renameDocument(
            @PathVariable UUID id,
            @RequestParam String title) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.renameDocument(id, title, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new DocumentResponse(doc));
    }

    @PostMapping("/{id}/move")
    @RequirePermission("documents:update")
    public ResponseEntity<DocumentResponse> moveDocument(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID targetFolderId) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.moveDocument(id, targetFolderId, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new DocumentResponse(doc));
    }

    @PostMapping("/{id}/archive")
    @RequirePermission("documents:archive")
    public ResponseEntity<Void> archiveDocument(@PathVariable UUID id) {
        AuthorizationContext ctx = getSecurityContext();
        documentService.archiveDocument(id, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @RequirePermission("documents:restore")
    public ResponseEntity<DocumentResponse> restoreDocument(@PathVariable UUID id) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.restoreDocument(id, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new DocumentResponse(doc));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("documents:delete")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        AuthorizationContext ctx = getSecurityContext();
        documentService.deleteDocument(id, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/metadata")
    @RequirePermission("documents:update")
    public ResponseEntity<DocumentResponse> updateMetadata(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentMetadataUpdateRequest request) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.updateMetadata(id, request.getMetadata(), ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new DocumentResponse(doc));
    }

    @PostMapping("/{id}/owner")
    @RequirePermission("documents:update")
    public ResponseEntity<DocumentResponse> changeOwnership(
            @PathVariable UUID id,
            @RequestParam UUID newOwnerId) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.changeOwnership(id, newOwnerId, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new DocumentResponse(doc));
    }

    @PostMapping("/{id}/tags")
    @RequirePermission("documents:update")
    public ResponseEntity<DocumentResponse> assignTags(
            @PathVariable UUID id,
            @RequestBody Set<UUID> tagIds) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.assignTags(id, tagIds, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new DocumentResponse(doc));
    }

    @DeleteMapping("/{id}/tags")
    @RequirePermission("documents:update")
    public ResponseEntity<DocumentResponse> removeTags(
            @PathVariable UUID id,
            @RequestBody Set<UUID> tagIds) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.removeTags(id, tagIds, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(new DocumentResponse(doc));
    }

    @GetMapping("/{id}")
    @RequirePermission("documents:read")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable UUID id) {
        AuthorizationContext ctx = getSecurityContext();
        Document doc = documentService.getDocumentOrThrow(id, ctx.getTenantId());
        return ResponseEntity.ok(new DocumentResponse(doc));
    }

    @GetMapping
    @RequirePermission("documents:read")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @RequestParam(required = false) UUID folderId,
            Pageable pageable) {
        AuthorizationContext ctx = getSecurityContext();
        Page<Document> docs = documentService.getDocuments(folderId, ctx.getTenantId(), pageable);
        List<DocumentResponse> response = docs.getContent().stream()
                .map(DocumentResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-metadata")
    @RequirePermission("documents:read")
    public ResponseEntity<List<DocumentResponse>> searchByMetadata(
            @RequestParam String key,
            @RequestParam String value,
            Pageable pageable) {
        AuthorizationContext ctx = getSecurityContext();
        Page<Document> docs = documentService.searchByMetadata(key, value, ctx.getTenantId(), pageable);
        List<DocumentResponse> response = docs.getContent().stream()
                .map(DocumentResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/versions")
    @RequirePermission("documents:read")
    public ResponseEntity<VersionHistoryDto> getVersionHistory(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AuthorizationContext ctx = getSecurityContext();
        VersionHistoryDto history = documentService.getVersionHistory(id, ctx.getTenantId(), ctx.getUserId(), page, size);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    @RequirePermission("documents:read")
    public ResponseEntity<LightweightVersionDto> getVersion(
            @PathVariable UUID id,
            @PathVariable int versionNumber) {
        AuthorizationContext ctx = getSecurityContext();
        LightweightVersionDto version = documentService.getVersion(id, versionNumber, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(version);
    }

    @GetMapping("/{id}/versions/{versionNumber}/download")
    @RequirePermission("documents:read")
    public ResponseEntity<?> downloadVersion(
            @PathVariable UUID id,
            @PathVariable int versionNumber,
            @RequestHeader HttpHeaders headers) {
        AuthorizationContext ctx = getSecurityContext();
        LightweightVersionDto version = documentService.getVersion(id, versionNumber, ctx.getTenantId(), ctx.getUserId());
        StorageResource resource = documentService.downloadVersion(id, versionNumber, ctx.getTenantId(), ctx.getUserId());
        return streamResource(resource, version.filename(), headers);
    }

    @GetMapping("/{id}/download")
    @RequirePermission("documents:read")
    public ResponseEntity<?> downloadLatestVersion(
            @PathVariable UUID id,
            @RequestHeader HttpHeaders headers) {
        AuthorizationContext ctx = getSecurityContext();
        LightweightVersionDto latest = documentService.getLatestVersion(id, ctx.getTenantId(), ctx.getUserId());
        StorageResource resource = documentService.downloadVersion(id, latest.versionNumber(), ctx.getTenantId(), ctx.getUserId());
        return streamResource(resource, latest.filename(), headers);
    }

    private ResponseEntity<?> streamResource(StorageResource resource, String filename, HttpHeaders headers) {
        org.springframework.core.io.Resource springResource = 
                new org.springframework.core.io.InputStreamResource(resource.inputStream()) {
                    @Override
                    public long contentLength() {
                        return resource.metadata().sizeBytes();
                    }
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                };

        try {
            List<HttpRange> ranges = headers.getRange();
            if (!ranges.isEmpty()) {
                HttpRange range = ranges.get(0);
                long start = range.getRangeStart(resource.metadata().sizeBytes());
                long end = range.getRangeEnd(resource.metadata().sizeBytes());
                long rangeLength = end - start + 1;
                
                InputStream is = resource.inputStream();
                long skipped = is.skip(start);
                InputStream rangeStream = new RangeInputStream(is, rangeLength);
                org.springframework.core.io.Resource rangeResource = new org.springframework.core.io.InputStreamResource(rangeStream);
                
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + resource.metadata().sizeBytes())
                        .header(HttpHeaders.ETAG, "\"" + resource.metadata().checksum() + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                        .contentLength(rangeLength)
                        .contentType(MediaType.parseMediaType(resource.metadata().mimeType()))
                        .body(rangeResource);
            }
        } catch (Exception e) {
            // Fallback to full stream on range errors
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.ETAG, "\"" + resource.metadata().checksum() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .contentLength(resource.metadata().sizeBytes())
                .contentType(MediaType.parseMediaType(resource.metadata().mimeType()))
                .body(springResource);
    }

    private static class RangeInputStream extends InputStream {
        private final InputStream in;
        private long remaining;

        public RangeInputStream(InputStream in, long limit) {
            this.in = in;
            this.remaining = limit;
        }

        @Override
        public int read() throws java.io.IOException {
            if (remaining <= 0) {
                return -1;
            }
            int result = in.read();
            if (result != -1) {
                remaining--;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws java.io.IOException {
            if (remaining <= 0) {
                return -1;
            }
            int maxRead = (int) Math.min(len, remaining);
            int result = in.read(b, off, maxRead);
            if (result != -1) {
                remaining -= result;
            }
            return result;
        }

        @Override
        public void close() throws java.io.IOException {
            in.close();
        }
    }

    private AuthorizationContext getSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthorizationContext) {
            return (AuthorizationContext) auth.getPrincipal();
        }
        throw new IllegalStateException("Unauthenticated request");
    }
}
