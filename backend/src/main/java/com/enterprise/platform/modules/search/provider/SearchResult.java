package com.enterprise.platform.modules.search.provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SearchResult(
    List<Match> matches,
    long totalHits,
    Map<String, Map<String, Long>> facets
) {
    public record Match(
        UUID documentId,
        UUID versionId,
        String tenantId,
        String title,
        String filename,
        double score,
        List<String> highlights,
        Map<String, Object> pageReferences
    ) {}
}
