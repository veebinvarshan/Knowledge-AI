package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.core.config.properties.EmbeddingProperties;
import com.enterprise.platform.modules.embedding.domain.EmbeddingJobStatus;
import com.enterprise.platform.modules.embedding.infrastructure.health.EmbeddingHealthIndicator;
import com.enterprise.platform.modules.embedding.repository.EmbeddingJobRepository;
import com.enterprise.platform.modules.embedding.service.EmbeddingProviderResolver;
import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmbeddingHealthIndicatorTest {

    @Test
    void testEmbeddingHealthIndicatorReportingUp() {
        EmbeddingProperties properties = new EmbeddingProperties(true, "NOOP", "model", "v1", 3, 1000);
        EmbeddingProviderResolver resolver = mock(EmbeddingProviderResolver.class);
        QdrantSearchProvider qdrantProvider = mock(QdrantSearchProvider.class);
        EmbeddingJobRepository jobRepository = mock(EmbeddingJobRepository.class);

        when(qdrantProvider.isConnected()).thenReturn(true);
        when(resolver.resolve("NOOP")).thenReturn(mock(com.enterprise.platform.modules.embedding.provider.EmbeddingProvider.class));
        when(jobRepository.findAllByStatus(EmbeddingJobStatus.PENDING)).thenReturn(Collections.emptyList());
        when(jobRepository.findAllByStatus(EmbeddingJobStatus.RETRYING)).thenReturn(Collections.emptyList());

        org.springframework.beans.factory.ObjectProvider<QdrantSearchProvider> qdrantProviderProvider = mock(org.springframework.beans.factory.ObjectProvider.class);
        when(qdrantProviderProvider.getIfAvailable()).thenReturn(qdrantProvider);

        EmbeddingHealthIndicator indicator = new EmbeddingHealthIndicator(properties, resolver, qdrantProviderProvider, jobRepository);

        // WHEN
        Health health = indicator.health();

        // THEN
        assertEquals(Status.UP, health.getStatus());
    }
}
