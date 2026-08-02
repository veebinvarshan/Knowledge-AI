package com.enterprise.platform.modules.documents.upload.api;

import com.enterprise.platform.modules.authorization.annotation.RequirePermission;
import com.enterprise.platform.modules.authorization.domain.AuthorizationContext;
import com.enterprise.platform.modules.documents.upload.exception.UploadException;
import com.enterprise.platform.modules.documents.upload.service.UploadPipelineService;
import com.enterprise.platform.modules.documents.upload.service.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final UploadPipelineService uploadPipelineService;

    public UploadController(UploadPipelineService uploadPipelineService) {
        this.uploadPipelineService = uploadPipelineService;
    }

    @PostMapping
    @RequirePermission("documents:create")
    public ResponseEntity<UploadSessionDto> initializeSession(@Valid @RequestBody UploadSessionInitDto request) {
        AuthorizationContext ctx = getSecurityContext();
        UploadSessionDto session = uploadPipelineService.initializeSession(ctx.getTenantId(), ctx.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PostMapping("/{sessionId}/chunks")
    @RequirePermission("documents:create")
    public ResponseEntity<ChunkUploadResultDto> uploadChunk(
            @PathVariable UUID sessionId,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("sizeBytes") long sizeBytes,
            @RequestParam("checksum") String checksum,
            @RequestParam("file") MultipartFile file) {
        
        AuthorizationContext ctx = getSecurityContext();
        try {
            ChunkUploadResultDto result = uploadPipelineService.uploadChunk(
                    sessionId,
                    ctx.getTenantId(),
                    ctx.getUserId(),
                    chunkNumber,
                    file.getInputStream(),
                    sizeBytes,
                    checksum
            );
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new UploadException("Failed to read chunk payload stream: " + e.getMessage());
        }
    }

    @GetMapping("/{sessionId}")
    @RequirePermission("documents:create")
    public ResponseEntity<ResumeStatusDto> getResumeStatus(@PathVariable UUID sessionId) {
        AuthorizationContext ctx = getSecurityContext();
        ResumeStatusDto status = uploadPipelineService.getResumeStatus(sessionId, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/{sessionId}/finalize")
    @RequirePermission("documents:create")
    public ResponseEntity<UploadSessionDto> finalizeUpload(
            @PathVariable UUID sessionId,
            @RequestParam(name = "checksum", required = false) String clientChecksum) {
        
        AuthorizationContext ctx = getSecurityContext();
        UploadSessionDto session = uploadPipelineService.finalizeUpload(sessionId, ctx.getTenantId(), ctx.getUserId(), clientChecksum);
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/{sessionId}")
    @RequirePermission("documents:create")
    public ResponseEntity<UploadSessionDto> abortUpload(@PathVariable UUID sessionId) {
        AuthorizationContext ctx = getSecurityContext();
        UploadSessionDto session = uploadPipelineService.abortUpload(sessionId, ctx.getTenantId(), ctx.getUserId());
        return ResponseEntity.ok(session);
    }

    private AuthorizationContext getSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthorizationContext) {
            return (AuthorizationContext) auth.getPrincipal();
        }
        throw new IllegalStateException("Unauthenticated request");
    }
}
