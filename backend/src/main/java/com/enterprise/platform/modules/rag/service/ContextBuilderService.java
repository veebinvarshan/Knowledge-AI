package com.enterprise.platform.modules.rag.service;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContextBuilderService {

    public String buildContext(List<SemanticSearchResult.Match> matches, int maxTokens) {
        // Estimated character budget = maxTokens * 4
        int characterBudget = maxTokens * 4;
        StringBuilder contextBuilder = new StringBuilder();
        int currentLength = 0;

        for (SemanticSearchResult.Match match : matches) {
            String snippet = match.snippet();
            if (snippet == null || snippet.trim().isEmpty()) {
                continue;
            }

            String docReference = String.format("Source: %s (ID: %s)\nContent: %s\n\n",
                    match.filename(), match.documentId(), snippet);

            if (currentLength + docReference.length() > characterBudget) {
                // Compression: fit whatever remains
                int remainingBudget = characterBudget - currentLength;
                if (remainingBudget > 50) {
                    String partialSnippet = snippet.substring(0, Math.min(snippet.length(), remainingBudget - 50));
                    contextBuilder.append(String.format("Source: %s (ID: %s)\nContent: %s... [Truncated due to token budget]\n\n",
                            match.filename(), match.documentId(), partialSnippet));
                }
                break;
            }

            contextBuilder.append(docReference);
            currentLength += docReference.length();
        }

        return contextBuilder.toString().trim();
    }
}
