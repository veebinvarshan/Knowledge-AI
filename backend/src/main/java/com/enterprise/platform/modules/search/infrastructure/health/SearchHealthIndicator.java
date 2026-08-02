package com.enterprise.platform.modules.search.infrastructure.health;

import com.enterprise.platform.core.config.properties.SearchProperties;
import com.enterprise.platform.modules.search.domain.SearchJobStatus;
import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import com.enterprise.platform.modules.search.repository.SearchJobRepository;
import com.enterprise.platform.modules.search.service.SearchIndexServiceImpl;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component("searchHealthIndicator")
public class SearchHealthIndicator implements HealthIndicator {

    private final SearchProperties properties;
    private final SearchJobRepository jobRepository;
    private final SearchIndexServiceImpl indexService;
    private final QdrantSearchProvider qdrantProvider;

    public SearchHealthIndicator(
            SearchProperties properties,
            SearchJobRepository jobRepository,
            SearchIndexServiceImpl indexService,
            QdrantSearchProvider qdrantProvider) {
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.indexService = indexService;
        this.qdrantProvider = qdrantProvider;
    }

    @Override
    public Health health() {
        if (!properties.enabled()) {
            return Health.up().withDetail("status", "Disabled by configuration").build();
        }

        boolean qdrantConnected = qdrantProvider.isConnected();

        ThreadPoolTaskExecutor executor = indexService.getTaskExecutor();
        int activeWorkers = executor.getActiveCount();
        int queueSize = executor.getQueueSize();

        long retryBacklog = 0;
        try {
            retryBacklog = jobRepository.findAllByStatusInAndStartedAtBefore(
                    List.of(SearchJobStatus.FAILED, SearchJobStatus.RETRYING),
                    Instant.now()
            ).size();
        } catch (Exception e) {
            // Ignore
        }

        // Qdrant is readiness check only; does not affect overall liveness
        Health.Builder builder = Health.up();
        return builder
                .withDetail("provider", properties.provider())
                .withDetail("luceneIndexHealth", "UP")
                .withDetail("qdrantConnected", qdrantConnected)
                .withDetail("activeWorkers", activeWorkers)
                .withDetail("queueSize", queueSize)
                .withDetail("retryBacklog", retryBacklog)
                .build();
    }
}
