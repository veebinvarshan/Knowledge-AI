package com.enterprise.platform.modules.semanticsearch.domain;

import java.util.Map;
import java.util.UUID;

public record SemanticSearchRequest(
    String query,
    UUID userId,
    Map<String, Object> filters,
    int limit
) {}
