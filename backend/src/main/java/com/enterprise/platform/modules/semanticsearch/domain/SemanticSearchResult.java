package com.enterprise.platform.modules.semanticsearch.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SemanticSearchResult(
    List<Match> matches,
    long totalHits,
    Map<String, Map<String, Long>> facets
) {
    public record Match(
        UUID documentId,
        UUID versionId,
        String title,
        String filename,
        double semanticScore,
        Double hybridScore,
        String snippet,
        List<String> highlights,
        Map<String, Object> metadataSummary
    ) {}
}
