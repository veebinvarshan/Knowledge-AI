package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.semanticsearch.service.SemanticSearchService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConcurrentSemanticSearchTest {

    @Test
    void testConcurrentSearchThreadExecution() throws InterruptedException {
        SemanticSearchService mockService = mock(SemanticSearchService.class);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                mockService.search("tenant", "hash", null);
            });
        }

        executor.shutdown();
        boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue(terminated);
        verify(mockService, times(10)).search(eq("tenant"), eq("hash"), any());
    }
}
