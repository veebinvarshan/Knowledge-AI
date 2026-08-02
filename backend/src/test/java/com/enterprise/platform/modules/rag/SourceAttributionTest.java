package com.enterprise.platform.modules.rag;

import com.enterprise.platform.modules.rag.domain.RagResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SourceAttributionTest {

    @Test
    void testCitationAttribution() {
        UUID docId = UUID.randomUUID();
        UUID verId = UUID.randomUUID();
        RagResponse.Citation citation = new RagResponse.Citation(
                docId, verId, "Document title", "filename.txt", "snippet matched", 0.95
        );

        assertEquals(docId, citation.documentId());
        assertEquals(verId, citation.versionId());
        assertEquals("Document title", citation.title());
        assertEquals("filename.txt", citation.filename());
        assertEquals("snippet matched", citation.snippet());
        assertEquals(0.95, citation.score());
    }
}
