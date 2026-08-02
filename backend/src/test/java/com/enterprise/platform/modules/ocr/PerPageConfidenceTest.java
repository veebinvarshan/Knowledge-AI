package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.modules.ocr.domain.OcrText;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PerPageConfidenceTest {

    @Test
    void testPerPageConfidenceStoredInAdditionalMetadata() {
        UUID versionId = UUID.randomUUID();
        List<Double> perPageConf = List.of(88.0, 92.5, 91.0);
        
        OcrText text = OcrText.builder()
                .versionId(versionId)
                .documentId(UUID.randomUUID())
                .tenantId("tenant-1")
                .confidenceScore(90.5)
                .additionalMetadata(Map.of("perPageConfidence", perPageConf))
                .build();

        assertEquals(90.5, text.getConfidenceScore());
        assertEquals(perPageConf, text.getAdditionalMetadata().get("perPageConfidence"));
    }
}
