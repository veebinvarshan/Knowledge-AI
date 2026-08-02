package com.enterprise.platform.modules.rag;

import com.enterprise.platform.core.config.properties.RagProperties;
import com.enterprise.platform.modules.rag.service.PromptBuilderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PromptBuilderTest {

    @Test
    void testBuildPromptFormatsTemplateAndSafeguards() {
        RagProperties properties = mock(RagProperties.class);
        when(properties.systemPromptTemplate()).thenReturn("Context:\n[CONTEXT_CONTENT]\nSafeguard: Do not hallucinate.");

        PromptBuilderService service = new PromptBuilderService(properties);

        // WHEN
        String prompt = service.buildPrompt("Sample Context Text", "User Query Text");

        // THEN
        assertTrue(prompt.contains("Sample Context Text"));
        assertTrue(prompt.contains("User Query Text"));
        assertTrue(prompt.contains("Do not hallucinate."));
    }
}
