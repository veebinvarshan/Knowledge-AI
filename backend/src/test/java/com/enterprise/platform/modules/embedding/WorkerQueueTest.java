package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.core.config.properties.EmbeddingWorkerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

public class WorkerQueueTest {

    @Test
    void testWorkerQueuePropertiesSetup() {
        EmbeddingWorkerProperties properties = new EmbeddingWorkerProperties(4, 100);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.threads());
        executor.setMaxPoolSize(properties.threads());
        executor.setQueueCapacity(properties.queueCapacity());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        assertEquals(4, executor.getCorePoolSize());
        executor.shutdown();
    }
}
