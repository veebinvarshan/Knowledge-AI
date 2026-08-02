package com.enterprise.platform.modules.virusscan.provider;

import com.enterprise.platform.core.config.properties.VirusScanProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VirusScannerProviderResolver {

    private final Map<String, VirusScannerProvider> providers;
    private final VirusScanProperties properties;

    public VirusScannerProviderResolver(List<VirusScannerProvider> providerList, VirusScanProperties properties) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(p -> p.getName().toUpperCase(), p -> p));
        this.properties = properties;
    }

    public VirusScanner resolve() {
        String name = properties.provider().toUpperCase();
        VirusScannerProvider provider = providers.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported virus scanner provider: " + name);
        }
        return provider.getScanner();
    }
}
