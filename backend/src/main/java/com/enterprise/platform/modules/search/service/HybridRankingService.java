package com.enterprise.platform.modules.search.service;

import com.enterprise.platform.modules.search.provider.SearchResult;

public interface HybridRankingService {
    SearchResult performRrf(SearchResult lexicalResult, SearchResult vectorResult, double bm25Weight, double vectorWeight, int k, int limit);
}
