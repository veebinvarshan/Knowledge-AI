package com.enterprise.platform.modules.semanticsearch;

import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import com.enterprise.platform.modules.search.repository.SearchDocumentRepository;
import com.enterprise.platform.modules.semanticsearch.provider.QdrantSemanticProvider;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QdrantSemanticProviderTest {

    @Test
    void testQdrantSemanticProviderReturnsSearchResult() throws Exception {
        QdrantSearchProvider mockBaseProvider = mock(QdrantSearchProvider.class);
        SearchDocumentRepository mockRepository = mock(SearchDocumentRepository.class);

        when(mockBaseProvider.isConnected()).thenReturn(false);
        when(mockRepository.findAllByTenantId("tenant-1")).thenReturn(List.of());

        org.springframework.beans.factory.ObjectProvider<QdrantSearchProvider> qdrantProviderProvider = mock(org.springframework.beans.factory.ObjectProvider.class);
        when(qdrantProviderProvider.getIfAvailable()).thenReturn(mockBaseProvider);

        QdrantSemanticProvider provider = new QdrantSemanticProvider(qdrantProviderProvider, mockRepository);

        // WHEN
        SemanticSearchResult result = provider.searchSemantic(new float[768], "tenant-1", new HashMap<>(), 10);

        // THEN
        assertNotNull(result);
        assertTrue(result.matches().isEmpty());
    }
}
