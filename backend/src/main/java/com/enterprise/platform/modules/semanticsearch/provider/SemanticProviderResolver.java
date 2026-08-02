package com.enterprise.platform.modules.semanticsearch.provider;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SemanticProviderResolver {

    private final List<SemanticSearchProvider> providers;

    public SemanticProviderResolver(List<SemanticSearchProvider> providers) {
        this.providers = providers;
    }

    public SemanticSearchProvider resolve(String providerName) {
        return providers.stream()
                .filter(p -> p.supports(providerName))
                .min(Comparator.comparingInt(SemanticSearchProvider::getPriority))
                .orElseThrow(() -> new IllegalArgumentException("No semantic search provider found supporting: " + providerName));
    }
}
