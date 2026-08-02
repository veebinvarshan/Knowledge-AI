package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.modules.ocr.domain.OcrText;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PageBoundaryPersistenceTest {

    @Test
    void testPageBoundariesConfiguredInBuilder() {
        UUID versionId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();
        
        Map<String, Object> boundaries = Map.of(
                "page_1", Map.of("start", 0, "end", 150),
                "page_2", Map.of("start", 151, "end", 300)
        );

        OcrText text = OcrText.builder()
                .versionId(versionId)
                .documentId(docId)
                .tenantId("tenant-1")
                .extractedText("Hello World Page 1\nHello World Page 2")
                .pageBoundaries(boundaries)
                .build();

        assertEquals(boundaries, text.getPageBoundaries());
        assertEquals("Hello World Page 1\nHello World Page 2", text.getExtractedText());
    }
}
