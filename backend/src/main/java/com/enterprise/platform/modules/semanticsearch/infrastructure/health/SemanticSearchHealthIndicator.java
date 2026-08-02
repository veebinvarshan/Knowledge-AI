package com.enterprise.platform.modules.semanticsearch.infrastructure.health;

import com.enterprise.platform.core.config.properties.SemanticSearchProperties;
import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import com.enterprise.platform.modules.semanticsearch.provider.SemanticProviderResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SemanticSearchHealthIndicator implements HealthIndicator {

    private final SemanticSearchProperties properties;
    private final SemanticProviderResolver resolver;
    private final QdrantSearchProvider qdrantSearchProvider;

    public SemanticSearchHealthIndicator(
            SemanticSearchProperties properties,
            SemanticProviderResolver resolver,
            ObjectProvider<QdrantSearchProvider> qdrantSearchProviderProvider) {
        this.properties = properties;
        this.resolver = resolver;
        this.qdrantSearchProvider = qdrantSearchProviderProvider.getIfAvailable();
    }

    @Override
    public Health health() {
        if (!properties.enabled()) {
            return Health.up().withDetail("status", "DISABLED").build();
        }

        boolean providerOk = false;
        try {
            providerOk = resolver.resolve(properties.provider()) != null;
        } catch (Exception ignored) {}

        boolean qdrantOk = qdrantSearchProvider != null && qdrantSearchProvider.isConnected();

        Health.Builder builder = Health.up()
                .withDetail("provider", properties.provider())
                .withDetail("providerAvailable", providerOk);

        if (qdrantSearchProvider != null) {
            builder.withDetail("qdrantConnected", qdrantOk);
        } else {
            builder.withDetail("qdrantConnected", "DISABLED");
        }

        builder.withDetail("averageSearchLatencyMs", 0)
               .withDetail("workerQueueDepth", 0);

        if (!providerOk || (qdrantSearchProvider != null && !qdrantOk)) {
            return builder.down().withDetail("error", "Semantic search dependencies offline").build();
        }

        return builder.build();
    }
}
