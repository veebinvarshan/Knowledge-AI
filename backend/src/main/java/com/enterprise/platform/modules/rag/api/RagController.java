package com.enterprise.platform.modules.rag.api;

import com.enterprise.platform.modules.rag.domain.RagRequest;
import com.enterprise.platform.modules.rag.domain.RagResponse;
import com.enterprise.platform.modules.rag.service.RagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/generate")
    public ResponseEntity<RagResponse> generate(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Permission-Hash") String permissionHash,
            @RequestBody RagRequest request) {

        RagResponse response = ragService.generate(tenantId, permissionHash, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStream(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Permission-Hash") String permissionHash,
            @RequestBody RagRequest request) {

        return ragService.generateStream(tenantId, permissionHash, request);
    }
}
