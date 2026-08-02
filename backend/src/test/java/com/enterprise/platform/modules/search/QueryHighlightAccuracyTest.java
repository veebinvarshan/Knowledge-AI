package com.enterprise.platform.modules.search;

import com.enterprise.platform.core.config.properties.LuceneProperties;
import com.enterprise.platform.modules.search.domain.SearchDocument;
import com.enterprise.platform.modules.search.provider.LuceneSearchProvider;
import com.enterprise.platform.modules.search.provider.SearchResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class QueryHighlightAccuracyTest {

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
    void testHighlightWrapsMatchedTerm() throws Exception {
        SearchDocument doc = SearchDocument.builder()
                .documentId(UUID.randomUUID())
                .versionId(UUID.randomUUID())
                .tenantId("tenant-1")
                .title("Annual Report")
                .normalizedText("this is the annual financial statement report of the company")
                .permissionHash("auth")
                .build();

        provider.index(doc);

        // WHEN
        SearchResult result = provider.search("financial", "tenant-1", "auth", 10);

        // THEN
        assertEquals(1, result.matches().size());
        List<String> highlights = result.matches().get(0).highlights();
        assertFalse(highlights.isEmpty());
        assertTrue(highlights.get(0).contains("<em>financial</em>"));
    }
}
