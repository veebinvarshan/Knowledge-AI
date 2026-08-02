package com.enterprise.platform.modules.semanticsearch.api;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchRequest;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;
import com.enterprise.platform.modules.semanticsearch.service.SemanticSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/semantic-search")
public class SemanticSearchController {

    private final SemanticSearchService searchService;

    public SemanticSearchController(SemanticSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<SemanticSearchResult> search(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Permission-Hash") String permissionHash,
            @RequestBody SemanticSearchRequest request) {
        
        SemanticSearchResult result = searchService.search(tenantId, permissionHash, request);
        return ResponseEntity.ok(result);
    }
}
