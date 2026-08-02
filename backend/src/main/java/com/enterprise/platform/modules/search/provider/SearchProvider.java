package com.enterprise.platform.modules.search.provider;

import com.enterprise.platform.modules.search.domain.SearchDocument;
import java.util.UUID;

public interface SearchProvider {
    boolean supports(String indexType);
    int getPriority(); // Priority selection order (lower is higher priority)
    void index(SearchDocument doc) throws Exception;
    void delete(UUID documentId) throws Exception;
    SearchResult search(String query, String tenantId, String permissionHash, int limit) throws Exception;
}
