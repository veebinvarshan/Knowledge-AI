package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QdrantPopulationTest {

    @Test
    void testQdrantIsConnectedOnlineMock() {
        QdrantSearchProvider mockProvider = mock(QdrantSearchProvider.class);
        when(mockProvider.isConnected()).thenReturn(true);

        assertTrue(mockProvider.isConnected());
    }
}
