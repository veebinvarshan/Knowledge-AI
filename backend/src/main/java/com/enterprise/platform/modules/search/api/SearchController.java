package com.enterprise.platform.modules.search.api;

import com.enterprise.platform.modules.search.provider.SearchResult;
import com.enterprise.platform.modules.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchResult> search(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Permission-Hash") String permissionHash,
            @RequestParam String query,
            @RequestParam(defaultValue = "hybrid") String searchType,
            @RequestParam(defaultValue = "10") int limit) throws Exception {

        SearchResult result = searchService.search(query, tenantId, permissionHash, searchType, limit);
        return ResponseEntity.ok(result);
    }
}
