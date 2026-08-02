package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.service.EmbeddingVersionService;
import com.enterprise.platform.modules.embedding.repository.EmbeddingChunkRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IncrementalEmbeddingTest {

    @Test
    void testIncrementalEmbeddingReturnsTrueIfDifferentChecksum() {
        EmbeddingChunkRepository mockRepository = mock(EmbeddingChunkRepository.class);
        EmbeddingVersionService service = new EmbeddingVersionService(mockRepository);

        UUID versionId = UUID.randomUUID();
        // Return empty collection meaning no chunks exist -> must trigger embedding
        when(mockRepository.findAllByVersionId(versionId)).thenReturn(Collections.emptyList());

        boolean stale = service.areEmbeddingsStale(versionId, "model", "v1", "new_checksum");
        assertTrue(stale);
    }
}
