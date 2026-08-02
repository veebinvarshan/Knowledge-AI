package com.enterprise.platform.modules.rag.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RagResponse(
    String text,
    List<Citation> citations,
    Map<String, Integer> tokenUsage,
    String status,
    long executionTimeMs
) {
    public record Citation(
        UUID documentId,
        UUID versionId,
        String title,
        String filename,
        String snippet,
        double score
    ) {}
}
