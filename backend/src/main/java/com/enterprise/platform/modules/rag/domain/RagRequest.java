package com.enterprise.platform.modules.rag.domain;

import java.util.Map;
import java.util.UUID;

public record RagRequest(
    String query,
    UUID userId,
    Map<String, Object> filters,
    String mode, // SEMANTIC, LEXICAL, HYBRID
    Integer maxContextTokens
) {}
