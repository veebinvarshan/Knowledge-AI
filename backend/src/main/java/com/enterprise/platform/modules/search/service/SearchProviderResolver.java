package com.enterprise.platform.modules.search.service;

import com.enterprise.platform.modules.search.provider.SearchProvider;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class SearchProviderResolver {

    private final List<SearchProvider> providers;

    public SearchProviderResolver(List<SearchProvider> providers) {
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(SearchProvider::getPriority))
                .toList();
    }

    public SearchProvider resolve(String indexType) {
        return providers.stream()
                .filter(p -> p.supports(indexType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No search provider found for index type: " + indexType));
    }

    public List<SearchProvider> getProviders() {
        return providers;
    }
}
