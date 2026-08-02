package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.repository.EmbeddingChunkRepository;
import com.enterprise.platform.modules.search.domain.SearchDocument;
import com.enterprise.platform.modules.search.repository.SearchDocumentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class EmbeddingReplacementTest {

    @Test
    void testTransactionalReplacementOrder() {
        EmbeddingChunkRepository chunkRepository = mock(EmbeddingChunkRepository.class);
        SearchDocumentRepository searchDocRepository = mock(SearchDocumentRepository.class);

        UUID versionId = UUID.randomUUID();
        SearchDocument originalDoc = SearchDocument.builder()
                .documentId(UUID.randomUUID())
                .versionId(versionId)
                .tenantId("tenant-1")
                .build();

        // WHEN: execute incremental re-embedding replacement
        chunkRepository.deleteAllByVersionId(versionId);
        searchDocRepository.deleteById(originalDoc.getDocumentId());
        searchDocRepository.save(originalDoc);

        // THEN: Deletion precedes save
        InOrder inOrder = Mockito.inOrder(chunkRepository, searchDocRepository);
        inOrder.verify(chunkRepository).deleteAllByVersionId(versionId);
        inOrder.verify(searchDocRepository).deleteById(originalDoc.getDocumentId());
        inOrder.verify(searchDocRepository).save(originalDoc);
    }
}
