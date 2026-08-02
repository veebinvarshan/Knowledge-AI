package com.enterprise.platform.modules.search;

import com.enterprise.platform.core.config.properties.LuceneProperties;
import com.enterprise.platform.modules.search.domain.SearchDocument;
import com.enterprise.platform.modules.search.provider.LuceneSearchProvider;
import com.enterprise.platform.modules.search.provider.SearchResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IncrementalIndexReplacementTest {

    private LuceneSearchProvider provider;

    @BeforeEach
    void setUp() {
        LuceneProperties properties = new LuceneProperties("mem");
        provider = new LuceneSearchProvider(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        provider.close();
    }

    @Test
    void testIncrementalIndexReplacesOldDocumentVersion() throws Exception {
        UUID docId = UUID.randomUUID();
        UUID versionId1 = UUID.randomUUID();
        SearchDocument doc1 = SearchDocument.builder()
                .documentId(docId)
                .versionId(versionId1)
                .tenantId("tenant-1")
                .title("Draft Policy version 1")
                .normalizedText("draft policy version 1")
                .permissionHash("auth")
                .build();

        provider.index(doc1);

        // WHEN: Re-index docId with a new version reference
        UUID versionId2 = UUID.randomUUID();
        SearchDocument doc2 = SearchDocument.builder()
                .documentId(docId)
                .versionId(versionId2)
                .tenantId("tenant-1")
                .title("Draft Policy version 2")
                .normalizedText("draft policy version 2")
                .permissionHash("auth")
                .build();

        provider.index(doc2);

        SearchResult result = provider.search("policy", "tenant-1", "auth", 10);

        // THEN: Old indexed document is replaced and matches only version 2 (Total hits must remain 1)
        assertEquals(1, result.matches().size());
        assertEquals(versionId2, result.matches().get(0).versionId());
    }
}
