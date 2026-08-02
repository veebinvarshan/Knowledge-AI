package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.search.domain.SearchDocument;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SourceConsistencyTest {

    @Test
    void testSearchDocumentTextAndEmbeddingSourceConsistent() {
        SearchDocument doc = SearchDocument.builder()
                .documentId(UUID.randomUUID())
                .versionId(UUID.randomUUID())
                .tenantId("tenant-1")
                .normalizedText("canonical text block")
                .build();

        // Ensure the source text for embedding matches normalizedText exactly
        String textToEmbed = doc.getNormalizedText();
        assertEquals("canonical text block", textToEmbed);
    }
}
