package com.enterprise.platform.modules.rag;

import com.enterprise.platform.core.config.properties.RagProperties;
import com.enterprise.platform.modules.rag.service.PromptBuilderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HallucinationSafeguardsTest {

    @Test
    void testSystemPromptSafeguardsHallucinations() {
        RagProperties properties = new RagProperties(
                true, "gemini-2.5-flash", 2000, 0.2, true,
                "Do not hallucinate and answer only based on context [CONTEXT_CONTENT]"
        );
        PromptBuilderService service = new PromptBuilderService(properties);

        // WHEN
        String prompt = service.buildPrompt("Sample Context", "Query");

        // THEN: verify constraints are part of prompt instruction
        assertTrue(prompt.contains("Do not hallucinate"));
        assertTrue(prompt.contains("Sample Context"));
    }
}
