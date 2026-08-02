package com.enterprise.platform.modules.embedding;

import com.enterprise.platform.core.config.properties.ChunkingProperties;
import com.enterprise.platform.modules.embedding.service.ChunkingService;
import com.enterprise.platform.modules.embedding.service.ChunkingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkDeterminismTest {

    @Test
    void testChunkingIsDeterministic() {
        ChunkingProperties properties = new ChunkingProperties("HYBRID", 100, 10);
        ChunkingService service = new ChunkingService(properties);

        String text = "Sample text to chunk. It has multiple sentences to verify boundaries.";

        // WHEN: Chunk twice
        List<ChunkingService.Chunk> chunks1 = service.chunkText(text, ChunkingStrategy.HYBRID);
        List<ChunkingService.Chunk> chunks2 = service.chunkText(text, ChunkingStrategy.HYBRID);

        // THEN
        assertEquals(chunks1.size(), chunks2.size());
        for (int i = 0; i < chunks1.size(); i++) {
            assertEquals(chunks1.get(i).text(), chunks2.get(i).text());
            assertEquals(chunks1.get(i).startOffset(), chunks2.get(i).startOffset());
            assertEquals(chunks1.get(i).endOffset(), chunks2.get(i).endOffset());
        }
    }
}
