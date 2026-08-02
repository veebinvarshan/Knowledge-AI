package com.enterprise.platform.modules.search.service;

import com.enterprise.platform.modules.search.provider.SearchResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HybridRankingServiceImpl implements HybridRankingService {

    @Override
    public SearchResult performRrf(
            SearchResult lexicalResult, 
            SearchResult vectorResult, 
            double bm25Weight, 
            double vectorWeight, 
            int k, 
            int limit) {
        
        Map<UUID, SearchResult.Match> matchMap = new HashMap<>();
        Map<UUID, Double> scoreMap = new HashMap<>();

        // 1. Lexical (BM25)
        List<SearchResult.Match> bm25Matches = lexicalResult.matches();
        for (int rank = 0; rank < bm25Matches.size(); rank++) {
            SearchResult.Match match = bm25Matches.get(rank);
            double score = bm25Weight / (k + rank);
            matchMap.put(match.documentId(), match);
            scoreMap.put(match.documentId(), score);
        }

        // 2. Semantic (Vector)
        List<SearchResult.Match> vectorMatches = vectorResult.matches();
        for (int rank = 0; rank < vectorMatches.size(); rank++) {
            SearchResult.Match match = vectorMatches.get(rank);
            double score = vectorWeight / (k + rank);
            matchMap.put(match.documentId(), match);
            scoreMap.put(match.documentId(), scoreMap.getOrDefault(match.documentId(), 0.0) + score);
        }

        // Sort by RRF score descending
        List<UUID> sortedKeys = new ArrayList<>(scoreMap.keySet());
        sortedKeys.sort((a, b) -> Double.compare(scoreMap.get(b), scoreMap.get(a)));

        List<SearchResult.Match> finalMatches = new ArrayList<>();
        int count = 0;
        for (UUID docId : sortedKeys) {
            if (count >= limit) break;
            SearchResult.Match baseMatch = matchMap.get(docId);
            finalMatches.add(new SearchResult.Match(
                    baseMatch.documentId(),
                    baseMatch.versionId(),
                    baseMatch.tenantId(),
                    baseMatch.title(),
                    baseMatch.filename(),
                    scoreMap.get(docId),
                    baseMatch.highlights(),
                    baseMatch.pageReferences()
            ));
            count++;
        }

        // Merge facets (base on lexical)
        Map<String, Map<String, Long>> mergedFacets = new HashMap<>(lexicalResult.facets());

        return new SearchResult(finalMatches, lexicalResult.totalHits(), mergedFacets);
    }
}
