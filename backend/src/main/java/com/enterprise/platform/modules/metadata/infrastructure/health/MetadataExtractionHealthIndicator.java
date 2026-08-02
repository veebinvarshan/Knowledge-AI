package com.enterprise.platform.modules.metadata.infrastructure.health;

import com.enterprise.platform.core.config.properties.MetadataProperties;
import com.enterprise.platform.modules.metadata.domain.MetadataJobStatus;
import com.enterprise.platform.modules.metadata.repository.MetadataJobRepository;
import com.enterprise.platform.modules.metadata.service.MetadataExtractionServiceImpl;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component("metadataExtractionHealthIndicator")
public class MetadataExtractionHealthIndicator implements HealthIndicator {

    private final MetadataProperties properties;
    private final MetadataJobRepository jobRepository;
    private final MetadataExtractionServiceImpl extractionService;

    public MetadataExtractionHealthIndicator(
            MetadataProperties properties,
            MetadataJobRepository jobRepository,
            MetadataExtractionServiceImpl extractionService) {
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.extractionService = extractionService;
    }

    @Override
    public Health health() {
        if (!properties.enabled()) {
            return Health.up().withDetail("status", "Disabled by configuration").build();
        }

        boolean parserAvailable = false;
        try {
            new AutoDetectParser();
            parserAvailable = true;
        } catch (Throwable t) {
            // Ignore instantiation errors for health flag
        }

        ThreadPoolTaskExecutor executor = extractionService.getTaskExecutor();
        int activeWorkers = executor.getActiveCount();
        int queueSize = executor.getQueueSize();
        
        long retryBacklog = 0;
        try {
            retryBacklog = jobRepository.findAllByStatusInAndStartedAtBefore(
                    List.of(MetadataJobStatus.FAILED, MetadataJobStatus.RETRYING),
                    Instant.now()
            ).size();
        } catch (Exception e) {
            // Safe fallback
        }

        Health.Builder builder = parserAvailable ? Health.up() : Health.down();
        return builder
                .withDetail("parserAvailable", parserAvailable)
                .withDetail("activeWorkers", activeWorkers)
                .withDetail("queueSize", queueSize)
                .withDetail("retryBacklog", retryBacklog)
                .build();
    }
}
