package com.enterprise.platform.modules.search;

import com.enterprise.platform.core.config.properties.SearchProperties;
import com.enterprise.platform.modules.search.provider.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HybridFallbackTest {

    @Test
    void testHybridSearchFallsBackToBm25Only() throws Exception {
        LuceneSearchProvider luceneProvider = mock(LuceneSearchProvider.class);
        QdrantSearchProvider qdrantProvider = mock(QdrantSearchProvider.class);
        SearchProperties properties = new SearchProperties(true, "HYBRID", 3, 1000, 1.0, 1.0, 60);

        UUID docId = UUID.randomUUID();
        SearchResult bm25Result = new SearchResult(
                List.of(new SearchResult.Match(docId, UUID.randomUUID(), "tenant-1", "doc.pdf", "doc.pdf", 5.0, List.of("highlight"), new HashMap<>())),
                1,
                new HashMap<>()
        );
        // Qdrant returns empty result (simulating NOT_GENERATED vectors)
        SearchResult vectorResult = new SearchResult(Collections.emptyList(), 0, new HashMap<>());

        when(luceneProvider.search("test", "tenant-1", "hash", 10)).thenReturn(bm25Result);
        when(qdrantProvider.search("test", "tenant-1", "hash", 10)).thenReturn(vectorResult);

        HybridSearchProvider hybridProvider = new HybridSearchProvider(
                luceneProvider, 
                qdrantProvider, 
                properties, 
                new com.enterprise.platform.modules.search.service.HybridRankingServiceImpl()
        );

        // WHEN
        SearchResult result = hybridProvider.search("test", "tenant-1", "hash", 10);

        // THEN: Executes cleanly without exceptions and outputs RRF-derived score (1.0 / (60 + 0) = 0.0166)
        assertEquals(1, result.matches().size());
        assertEquals(docId, result.matches().get(0).documentId());
        assertTrue(result.matches().get(0).score() > 0.0);
    }
}
