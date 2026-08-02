package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.domain.EmbeddingJob;
import com.enterprise.platform.modules.embedding.repository.EmbeddingJobRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConcurrentEmbeddingGenerationTest {

    @Test
    void testConcurrentExecutionMockDoesNotFail() throws InterruptedException {
        EmbeddingJobRepository mockRepository = mock(EmbeddingJobRepository.class);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                EmbeddingJob job = new EmbeddingJob(UUID.randomUUID(), UUID.randomUUID(), "tenant-1", "GEMINI", "model", "v1");
                mockRepository.save(job);
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue(finished);
        verify(mockRepository, atLeastOnce()).save(any(EmbeddingJob.class));
    }
}
