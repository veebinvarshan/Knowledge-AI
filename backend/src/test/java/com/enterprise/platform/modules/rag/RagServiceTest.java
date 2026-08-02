package com.enterprise.platform.modules.rag;

import com.enterprise.platform.core.config.properties.RagProperties;
import com.enterprise.platform.modules.rag.domain.*;
import com.enterprise.platform.modules.rag.repository.RagAnalyticsRepository;
import com.enterprise.platform.modules.rag.service.*;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;
import com.enterprise.platform.modules.semanticsearch.service.SemanticSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RagServiceTest {

    @Test
    void testRagGenerateSuccess() {
        RagAnalyticsRepository repo = mock(RagAnalyticsRepository.class);
        SemanticSearchService searchService = mock(SemanticSearchService.class);
        ContextBuilderService contextBuilder = mock(ContextBuilderService.class);
        PromptBuilderService promptBuilder = mock(PromptBuilderService.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ObjectProvider<ChatModel> chatModelProvider = mock(ObjectProvider.class);

        RagProperties properties = new RagProperties(
                true, "gemini-2.5-flash", 2000, 0.2, true, "system prompt [CONTEXT_CONTENT]"
        );

        when(repo.save(any(RagJob.class))).thenAnswer(i -> i.getArguments()[0]);
        when(searchService.search(any(), any(), any())).thenReturn(
                new SemanticSearchResult(Collections.emptyList(), 0, Collections.emptyMap())
        );
        when(contextBuilder.buildContext(any(), anyInt())).thenReturn("context string");
        when(promptBuilder.buildPrompt(any(), any())).thenReturn("prompt string");

        RagServiceImpl service = new RagServiceImpl(
                repo, searchService, contextBuilder, promptBuilder, publisher, chatModelProvider, properties
        );

        RagRequest req = new RagRequest("query", UUID.randomUUID(), new HashMap<>(), "HYBRID", 2000);

        // WHEN
        RagResponse response = service.generate("tenant-1", "hash", req);

        // THEN
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
    }
}
