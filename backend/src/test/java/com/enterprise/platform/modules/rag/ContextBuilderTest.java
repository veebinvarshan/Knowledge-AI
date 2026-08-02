package com.enterprise.platform.modules.rag;

import com.enterprise.platform.modules.rag.service.ContextBuilderService;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ContextBuilderTest {

    @Test
    void testBuildContextRespectsTokenBudgetAndTruncates() {
        ContextBuilderService service = new ContextBuilderService();
        SemanticSearchResult.Match m1 = new SemanticSearchResult.Match(
                UUID.randomUUID(), UUID.randomUUID(), "Doc 1", "file1.txt", 0.9, null,
                "This is a long piece of snippet context to check budgeting limits.", List.of(), new HashMap<>()
        );
        SemanticSearchResult.Match m2 = new SemanticSearchResult.Match(
                UUID.randomUUID(), UUID.randomUUID(), "Doc 2", "file2.txt", 0.8, null,
                "This is another piece of context content.", List.of(), new HashMap<>()
        );

        // Budget of 25 tokens (100 characters)
        String context = service.buildContext(List.of(m1, m2), 25);

        assertNotNull(context);
        assertTrue(context.contains("file1.txt"));
        // Second match should not be fully added or truncated since it exceeds character budget of 100
        assertFalse(context.contains("file2.txt"));
    }
}
