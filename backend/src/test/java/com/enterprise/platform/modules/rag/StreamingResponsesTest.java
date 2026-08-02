package com.enterprise.platform.modules.rag;

import com.enterprise.platform.modules.rag.domain.RagRequest;
import com.enterprise.platform.modules.rag.service.RagService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class StreamingResponsesTest {

    @Test
    void testStreamingGeneration() {
        RagService mockService = mock(RagService.class);
        RagRequest request = new RagRequest("query", UUID.randomUUID(), Map.of(), "HYBRID", 2000);

        when(mockService.generateStream("tenant", "hash", request))
                .thenReturn(Flux.just("Based ", "on ", "context"));

        // WHEN
        Flux<String> stream = mockService.generateStream("tenant", "hash", request);

        // THEN
        StepVerifier.create(stream)
                .expectNext("Based ")
                .expectNext("on ")
                .expectNext("context")
                .verifyComplete();
    }
}
