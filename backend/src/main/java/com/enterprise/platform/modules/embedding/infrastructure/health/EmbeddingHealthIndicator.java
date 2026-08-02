package com.enterprise.platform.modules.embedding.infrastructure.health;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.modules.embedding.domain.EmbeddingJobStatus;
import com.enterprise.platform.modules.embedding.repository.EmbeddingJobRepository;
import com.enterprise.platform.modules.embedding.service.EmbeddingProviderResolver;
import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingHealthIndicator implements HealthIndicator {

    private final EmbeddingProperties properties;
    private final EmbeddingProviderResolver resolver;
    private final QdrantSearchProvider qdrantSearchProvider;
    private final EmbeddingJobRepository jobRepository;

    public EmbeddingHealthIndicator(
            EmbeddingProperties properties,
            EmbeddingProviderResolver resolver,
            ObjectProvider<QdrantSearchProvider> qdrantSearchProviderProvider,
            EmbeddingJobRepository jobRepository) {
        this.properties = properties;
        this.resolver = resolver;
        this.qdrantSearchProvider = qdrantSearchProviderProvider.getIfAvailable();
        this.jobRepository = jobRepository;
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
        long pendingJobs = jobRepository.findAllByStatus(EmbeddingJobStatus.PENDING).size();
        long retryingJobs = jobRepository.findAllByStatus(EmbeddingJobStatus.RETRYING).size();

        Health.Builder builder = Health.up()
                .withDetail("provider", properties.provider())
                .withDetail("providerAvailable", providerOk)
                .withDetail("pendingJobs", pendingJobs)
                .withDetail("retryBacklog", retryingJobs);

        if (qdrantSearchProvider != null) {
            builder.withDetail("qdrantConnected", qdrantOk);
        } else {
            builder.withDetail("qdrantConnected", "DISABLED");
        }

        if (!providerOk || (qdrantSearchProvider != null && !qdrantOk)) {
            return builder.down().withDetail("error", "Dependency connectivity down").build();
        }

        return builder.build();
    }
}
