package com.enterprise.platform.modules.search;

import com.enterprise.platform.core.config.properties.SearchProperties;
import com.enterprise.platform.modules.search.provider.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RrfRankingAccuracyTest {

    @Test
    void testRrfRanksDeterministicScores() throws Exception {
        LuceneSearchProvider luceneProvider = mock(LuceneSearchProvider.class);
        QdrantSearchProvider qdrantProvider = mock(QdrantSearchProvider.class);
        SearchProperties properties = new SearchProperties(true, "HYBRID", 3, 1000, 1.0, 1.0, 60);

        UUID docA = UUID.randomUUID();
        UUID docB = UUID.randomUUID();

        // BM25 results: A is rank 0, B is rank 1
        SearchResult bm25Result = new SearchResult(
                List.of(
                        new SearchResult.Match(docA, UUID.randomUUID(), "tenant-1", "A", "A", 4.0, List.of(), new HashMap<>()),
                        new SearchResult.Match(docB, UUID.randomUUID(), "tenant-1", "B", "B", 3.0, List.of(), new HashMap<>())
                ),
                2,
                new HashMap<>()
        );

        // Vector results: B is rank 0, A is rank 1
        SearchResult vectorResult = new SearchResult(
                List.of(
                        new SearchResult.Match(docB, UUID.randomUUID(), "tenant-1", "B", "B", 0.9, List.of(), new HashMap<>()),
                        new SearchResult.Match(docA, UUID.randomUUID(), "tenant-1", "A", "A", 0.8, List.of(), new HashMap<>())
                ),
                2,
                new HashMap<>()
        );

        when(luceneProvider.search("query", "tenant-1", "hash", 10)).thenReturn(bm25Result);
        when(qdrantProvider.search("query", "tenant-1", "hash", 10)).thenReturn(vectorResult);

        HybridSearchProvider provider = new HybridSearchProvider(
                luceneProvider, 
                qdrantProvider, 
                properties, 
                new com.enterprise.platform.modules.search.service.HybridRankingServiceImpl()
        );

        // WHEN
        SearchResult result = provider.search("query", "tenant-1", "hash", 10);

        // THEN: Both score equal RRF sums (1/(60+0) + 1/(60+1) = 0.01666 + 0.01639 = 0.03305)
        assertEquals(2, result.matches().size());
        assertTrue(result.matches().get(0).score() > 0.033);
        assertTrue(result.matches().get(1).score() > 0.033);
    }
}
