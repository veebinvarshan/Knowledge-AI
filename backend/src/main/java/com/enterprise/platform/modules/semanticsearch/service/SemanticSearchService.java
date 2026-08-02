package com.enterprise.platform.modules.semanticsearch.service;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchRequest;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;

public interface SemanticSearchService {
    SemanticSearchResult search(String tenantId, String permissionHash, SemanticSearchRequest request);
}
