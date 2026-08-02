package com.enterprise.platform.modules.search;

import com.enterprise.platform.modules.search.provider.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PageReferenceHighlightTest {

    @Test
    void testPageReferencesCanBeStored() {
        Map<String, Object> pageRefs = Map.of(
                "page_1", Map.of("start", 0, "end", 200)
        );

        SearchResult.Match match = new SearchResult.Match(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "tenant-1",
                "pdf.pdf",
                "pdf.pdf",
                0.9,
                List.of("hl"),
                pageRefs
        );

        assertEquals(pageRefs, match.pageReferences());
    }
}
