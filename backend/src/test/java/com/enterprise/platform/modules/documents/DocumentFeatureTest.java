package com.enterprise.platform.modules.documents;

import com.enterprise.platform.modules.documents.domain.DocumentVersion;
import com.enterprise.platform.modules.documents.domain.DuplicateDetectionStrategy;
import com.enterprise.platform.modules.documents.exception.DuplicateDocumentException;
import com.enterprise.platform.modules.documents.repository.DocumentVersionRepository;
import com.enterprise.platform.modules.documents.service.DuplicateDetectionServiceImpl;
import com.enterprise.platform.modules.documents.service.dto.DuplicateDetectionResult;
import com.enterprise.platform.modules.search.provider.SearchResult;
import com.enterprise.platform.modules.search.service.SearchProviderResolver;
import com.enterprise.platform.core.config.properties.SearchProperties;
import com.enterprise.platform.modules.search.service.SearchServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DocumentFeatureTest {

    @Test
    void testDuplicateDetectionRejectStrategy() {
        DocumentVersionRepository versionRepo = mock(DocumentVersionRepository.class);
        DuplicateDetectionServiceImpl duplicateService = new DuplicateDetectionServiceImpl(versionRepo, "REJECT_DUPLICATE");

        String tenantId = "test-tenant";
        String checksum = "hash123";

        // GIVEN: duplicate exists
        DocumentVersion existingVersion = mock(DocumentVersion.class);
        com.enterprise.platform.modules.documents.domain.Document doc = mock(com.enterprise.platform.modules.documents.domain.Document.class);
        UUID docId = UUID.randomUUID();
        UUID verId = UUID.randomUUID();
        when(doc.getId()).thenReturn(docId);
        when(existingVersion.getDocument()).thenReturn(doc);
        when(existingVersion.getId()).thenReturn(verId);
        when(versionRepo.findActiveVersionsByChecksum(tenantId, checksum)).thenReturn(List.of(existingVersion));

        // WHEN
        DuplicateDetectionResult result = duplicateService.evaluateDuplicate(tenantId, checksum, "file.pdf", UUID.randomUUID());

        // THEN
        assertTrue(result.duplicateFound());
        assertEquals(DuplicateDetectionStrategy.REJECT_DUPLICATE, result.selectedStrategy());
        assertEquals("REJECT", result.recommendedAction());
        assertEquals(docId, result.existingDocumentId());
    }

    @Test
    void testDuplicateDetectionAllowStrategy() {
        DocumentVersionRepository versionRepo = mock(DocumentVersionRepository.class);
        DuplicateDetectionServiceImpl duplicateService = new DuplicateDetectionServiceImpl(versionRepo, "ALLOW_DUPLICATE");

        String tenantId = "test-tenant";
        String checksum = "hash123";

        // WHEN
        DuplicateDetectionResult result = duplicateService.evaluateDuplicate(tenantId, checksum, "file.pdf", UUID.randomUUID());

        // THEN
        assertFalse(result.duplicateFound());
        assertEquals(DuplicateDetectionStrategy.ALLOW_DUPLICATE, result.selectedStrategy());
        assertEquals("PROCEED", result.recommendedAction());
        verifyNoInteractions(versionRepo);
    }

    @Test
    void testSearchTypeMappingToProviders() throws Exception {
        SearchProviderResolver resolver = mock(SearchProviderResolver.class);
        SearchProperties properties = mock(SearchProperties.class);
        SearchServiceImpl searchService = new SearchServiceImpl(resolver, properties);

        com.enterprise.platform.modules.search.provider.SearchProvider mockLucene = mock(com.enterprise.platform.modules.search.provider.SearchProvider.class);
        com.enterprise.platform.modules.search.provider.SearchProvider mockQdrant = mock(com.enterprise.platform.modules.search.provider.SearchProvider.class);
        com.enterprise.platform.modules.search.provider.SearchProvider mockHybrid = mock(com.enterprise.platform.modules.search.provider.SearchProvider.class);

        when(resolver.resolve("LEXICAL")).thenReturn(mockLucene);
        when(resolver.resolve("VECTOR")).thenReturn(mockQdrant);
        when(resolver.resolve("HYBRID")).thenReturn(mockHybrid);

        SearchResult dummyResult = new SearchResult(Collections.emptyList(), 0, Collections.emptyMap());
        when(mockLucene.search(any(), any(), any(), anyInt())).thenReturn(dummyResult);
        when(mockQdrant.search(any(), any(), any(), anyInt())).thenReturn(dummyResult);
        when(mockHybrid.search(any(), any(), any(), anyInt())).thenReturn(dummyResult);

        // WHEN
        searchService.search("query", "tenant", "hash", "lexical", 10);
        searchService.search("query", "tenant", "hash", "semantic", 10);
        searchService.search("query", "tenant", "hash", "hybrid", 10);

        // THEN
        verify(resolver).resolve("LEXICAL");
        verify(resolver).resolve("VECTOR");
        verify(resolver).resolve("HYBRID");
    }
}
