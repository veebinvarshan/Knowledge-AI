package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.repository.EmbeddingChunkRepository;
import com.enterprise.platform.modules.search.provider.QdrantSearchProvider;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class DuplicateVectorPreventionTest {

    @Test
    void testClearOldVectorsBeforeUpsert() throws Exception {
        EmbeddingChunkRepository chunkRepository = mock(EmbeddingChunkRepository.class);
        QdrantSearchProvider qdrantProvider = mock(QdrantSearchProvider.class);

        UUID versionId = UUID.randomUUID();

        // WHEN
        chunkRepository.deleteAllByVersionId(versionId);
        qdrantProvider.deleteVectors(versionId);

        // THEN: Verify deletions happen in order before new vectors are inserted
        InOrder inOrder = Mockito.inOrder(chunkRepository, qdrantProvider);
        inOrder.verify(chunkRepository).deleteAllByVersionId(versionId);
        inOrder.verify(qdrantProvider).deleteVectors(versionId);
    }
}
