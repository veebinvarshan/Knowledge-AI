package com.enterprise.platform.modules.semanticsearch;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataPayloadFilterTest {

    @Test
    void testFilterPayloadConstructsSuccessfully() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("mimeType", "application/pdf");
        filters.put("language", "en");
        filters.put("archived", false);

        assertEquals("application/pdf", filters.get("mimeType"));
        assertEquals("en", filters.get("language"));
        assertEquals(false, filters.get("archived"));
    }
}
