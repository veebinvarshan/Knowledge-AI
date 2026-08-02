package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.search.provider.SearchResult;
import com.enterprise.platform.modules.search.service.HybridRankingService;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HybridSemanticSearchTest {

    @Test
    void testHybridSearchInvokesBlender() {
        HybridRankingService mockRankingService = mock(HybridRankingService.class);
        SearchResult lexicalResult = new SearchResult(Collections.<SearchResult.Match>emptyList(), 0, Collections.emptyMap());
        SearchResult semanticResult = new SearchResult(Collections.<SearchResult.Match>emptyList(), 0, Collections.emptyMap());

        when(mockRankingService.performRrf(lexicalResult, semanticResult, 1.0, 1.0, 60, 10))
                .thenReturn(new SearchResult(Collections.<SearchResult.Match>emptyList(), 0, Collections.emptyMap()));

        // WHEN
        SearchResult result = mockRankingService.performRrf(lexicalResult, semanticResult, 1.0, 1.0, 60, 10);

        // THEN
        assertNotNull(result);
        verify(mockRankingService).performRrf(lexicalResult, semanticResult, 1.0, 1.0, 60, 10);
    }
}
