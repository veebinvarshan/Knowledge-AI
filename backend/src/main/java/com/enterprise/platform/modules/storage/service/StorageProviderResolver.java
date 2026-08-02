package com.enterprise.platform.modules.storage.service;

import com.enterprise.platform.core.config.properties.StorageProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StorageProviderResolver {

    private final Map<String, StorageProvider> providersMap;
    private final StorageProperties properties;

    public StorageProviderResolver(List<StorageProvider> providers, StorageProperties properties) {
        this.providersMap = providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getProviderId().toUpperCase(),
                        p -> p
                ));
        this.properties = properties;
    }

    public StorageProvider resolveActiveProvider() {
        String activeProvider = properties.provider().toUpperCase();
        StorageProvider provider = providersMap.get(activeProvider);
        if (provider == null) {
            throw new IllegalArgumentException("No StorageProvider configured matching active provider ID: " + activeProvider);
        }
        return provider;
    }
}
