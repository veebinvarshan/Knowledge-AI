package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.search.domain.SearchDocument;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SearchDocumentConsistencyTest {

    @Test
    void testSearchDocumentUpdateKeepsMetadataConsistent() {
        SearchDocument original = SearchDocument.builder()
                .documentId(UUID.randomUUID())
                .versionId(UUID.randomUUID())
                .tenantId("tenant-1")
                .title("Original Title")
                .filename("doc.pdf")
                .mimeType("application/pdf")
                .language("en")
                .author("Author")
                .normalizedText("canonical text")
                .vectorStatus("NOT_GENERATED")
                .build();

        // WHEN: Re-build document after embedding completes
        SearchDocument updated = SearchDocument.builder()
                .documentId(original.getDocumentId())
                .versionId(original.getVersionId())
                .tenantId(original.getTenantId())
                .title(original.getTitle())
                .filename(original.getFilename())
                .mimeType(original.getMimeType())
                .language(original.getLanguage())
                .author(original.getAuthor())
                .normalizedText(original.getNormalizedText())
                .vectorStatus("GENERATED")
                .embeddingModel("model")
                .embeddingVersion("v1")
                .build();

        // THEN: Verify structural consistency holds
        assertEquals(original.getDocumentId(), updated.getDocumentId());
        assertEquals(original.getTitle(), updated.getTitle());
        assertEquals("GENERATED", updated.getVectorStatus());
    }
}
