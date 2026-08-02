package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.core.config.properties.MetadataProperties;
import com.enterprise.platform.modules.metadata.infrastructure.health.MetadataExtractionHealthIndicator;
import com.enterprise.platform.modules.metadata.repository.MetadataJobRepository;
import com.enterprise.platform.modules.metadata.service.MetadataExtractionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MetadataHealthIndicatorTest {

    @Test
    void testHealthIndicatorRetrievesExecutorStats() {
        // GIVEN
        MetadataProperties properties = new MetadataProperties(true, "TIKA", 3, 1000);
        MetadataJobRepository jobRepository = mock(MetadataJobRepository.class);
        MetadataExtractionServiceImpl service = mock(MetadataExtractionServiceImpl.class);
        ThreadPoolTaskExecutor taskExecutor = mock(ThreadPoolTaskExecutor.class);

        when(service.getTaskExecutor()).thenReturn(taskExecutor);
        when(taskExecutor.getActiveCount()).thenReturn(2);
        when(taskExecutor.getQueueSize()).thenReturn(15);

        MetadataExtractionHealthIndicator healthIndicator = new MetadataExtractionHealthIndicator(properties, jobRepository, service);

        // WHEN
        Health health = healthIndicator.health();

        // THEN
        assertEquals(Status.UP, health.getStatus());
        assertEquals(2, health.getDetails().get("activeWorkers"));
        assertEquals(15, health.getDetails().get("queueSize"));
    }
}
