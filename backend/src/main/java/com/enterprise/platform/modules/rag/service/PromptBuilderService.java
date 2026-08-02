package com.enterprise.platform.modules.rag.service;

import com.enterprise.platform.core.config.properties.RagProperties;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    private final RagProperties properties;

    public PromptBuilderService(RagProperties properties) {
        this.properties = properties;
    }

    public String buildPrompt(String context, String query) {
        String template = properties.systemPromptTemplate();
        String prompt = template.replace("[CONTEXT_CONTENT]", context);

        prompt += "\nQuery: " + query + "\n\nAnswer:";
        return prompt;
    }
}
