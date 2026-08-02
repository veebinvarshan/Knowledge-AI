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

public class AuthorizationIsolationTest {

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
    void testTenantIsolationAndPermissionPreFilter() throws Exception {
        UUID docId1 = UUID.randomUUID();
        SearchDocument doc1 = SearchDocument.builder()
                .documentId(docId1)
                .versionId(UUID.randomUUID())
                .tenantId("tenant-A")
                .title("Financial Statement tenant A")
                .normalizedText("financial statement tenant a")
                .permissionHash("auth_role")
                .build();

        UUID docId2 = UUID.randomUUID();
        SearchDocument doc2 = SearchDocument.builder()
                .documentId(docId2)
                .versionId(UUID.randomUUID())
                .tenantId("tenant-B")
                .title("Financial Statement tenant B")
                .normalizedText("financial statement tenant b")
                .permissionHash("auth_role")
                .build();

        provider.index(doc1);
        provider.index(doc2);

        // WHEN: Search query from Tenant A
        SearchResult resultA = provider.search("financial", "tenant-A", "auth_role", 10);

        // THEN: Only Tenant A document matches (Tenant B is completely isolated)
        assertEquals(1, resultA.matches().size());
        assertEquals(docId1, resultA.matches().get(0).documentId());
    }
}
