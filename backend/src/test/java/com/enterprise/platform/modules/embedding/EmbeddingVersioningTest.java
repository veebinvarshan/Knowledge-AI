package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.modules.embedding.domain.EmbeddingChunk;
import com.enterprise.platform.modules.embedding.repository.EmbeddingChunkRepository;
import com.enterprise.platform.modules.embedding.service.EmbeddingVersionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmbeddingVersioningTest {

    @Test
    void testStaleCheckReturnsTrueIfNoPreviousChunksExist() {
        EmbeddingChunkRepository mockRepository = mock(EmbeddingChunkRepository.class);
        EmbeddingVersionService service = new EmbeddingVersionService(mockRepository);

        UUID versionId = UUID.randomUUID();
        when(mockRepository.findAllByVersionId(versionId)).thenReturn(List.of());

        // WHEN
        boolean stale = service.areEmbeddingsStale(versionId, "model", "v1", "checksum");

        // THEN
        assertTrue(stale);
    }
}
