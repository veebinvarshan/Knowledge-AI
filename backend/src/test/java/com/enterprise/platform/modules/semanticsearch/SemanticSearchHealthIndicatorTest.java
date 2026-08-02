package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.core.config.properties.SemanticSearchProperties;
import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import com.enterprise.platform.modules.semanticsearch.infrastructure.health.SemanticSearchHealthIndicator;
import com.enterprise.platform.modules.semanticsearch.provider.SemanticProviderResolver;
import com.enterprise.platform.modules.semanticsearch.provider.SemanticSearchProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SemanticSearchHealthIndicatorTest {

    @Test
    void testIndicatorUp() {
        SemanticSearchProperties properties = new SemanticSearchProperties(true, "HYBRID", "QDRANT", "COSINE", 10, false, 60000);
        SemanticProviderResolver resolver = mock(SemanticProviderResolver.class);
        QdrantSearchProvider qdrantSearchProvider = mock(QdrantSearchProvider.class);

        when(qdrantSearchProvider.isConnected()).thenReturn(true);
        when(resolver.resolve("QDRANT")).thenReturn(mock(SemanticSearchProvider.class));

        org.springframework.beans.factory.ObjectProvider<QdrantSearchProvider> qdrantProviderProvider = mock(org.springframework.beans.factory.ObjectProvider.class);
        when(qdrantProviderProvider.getIfAvailable()).thenReturn(qdrantSearchProvider);

        SemanticSearchHealthIndicator indicator = new SemanticSearchHealthIndicator(properties, resolver, qdrantProviderProvider);

        // WHEN
        Health health = indicator.health();

        // THEN
        assertEquals(Status.UP, health.getStatus());
    }
}
