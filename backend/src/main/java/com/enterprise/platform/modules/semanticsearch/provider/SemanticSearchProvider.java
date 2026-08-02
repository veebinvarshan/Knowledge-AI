package com.enterprise.platform.modules.semanticsearch.provider;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;
import java.util.Map;

public interface SemanticSearchProvider {
    boolean supports(String provider);
    SemanticSearchResult searchSemantic(float[] queryVector, String tenantId, Map<String, Object> filters, int limit) throws Exception;
    int getPriority();
}
