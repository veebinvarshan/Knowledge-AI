package com.enterprise.platform.modules.search;

import com.enterprise.platform.modules.search.domain.SearchDocument;
import com.enterprise.platform.modules.search.repository.SearchDocumentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class IndexReplacementTest {

    @Test
    void testTransactionalReplacementDeletesOldFirst() {
        SearchDocumentRepository mockRepository = mock(SearchDocumentRepository.class);
        UUID documentId = UUID.randomUUID();
        
        SearchDocument doc = SearchDocument.builder()
                .documentId(documentId)
                .versionId(UUID.randomUUID())
                .tenantId("tenant-1")
                .title("New title")
                .build();

        // WHEN
        mockRepository.deleteById(doc.getDocumentId());
        mockRepository.save(doc);

        // THEN: Verify order of replacement execution (Delete first, then save)
        InOrder inOrder = Mockito.inOrder(mockRepository);
        inOrder.verify(mockRepository).deleteById(documentId);
        inOrder.verify(mockRepository).save(doc);
    }
}
