package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ResultMappingTest {

    @Test
    void testMatchFieldsExposedCorrectly() {
        SemanticSearchResult.Match match = new SemanticSearchResult.Match(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Title",
                "file.txt",
                0.9,
                1.8,
                "snippet text",
                List.of("highlight"),
                new HashMap<>()
        );

        // THEN: verify DTO mapping structure
        assertEquals("Title", match.title());
        assertEquals("file.txt", match.filename());
        assertEquals(0.9, match.semanticScore());
        assertEquals(1.8, match.hybridScore());
    }
}
