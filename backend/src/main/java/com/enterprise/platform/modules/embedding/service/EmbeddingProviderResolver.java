package com.enterprise.platform.modules.embedding.service;

import com.enterprise.platform.modules.embedding.provider.EmbeddingProvider;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class EmbeddingProviderResolver {

    private final List<EmbeddingProvider> providers;

    public EmbeddingProviderResolver(List<EmbeddingProvider> providers) {
        this.providers = providers;
    }

    public EmbeddingProvider resolve(String providerName) {
        return providers.stream()
                .filter(p -> p.supports(providerName))
                .min(Comparator.comparingInt(EmbeddingProvider::getPriority))
                .orElseThrow(() -> new IllegalArgumentException("No embedding provider found supporting: " + providerName));
    }
}
