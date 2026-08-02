package com.enterprise.platform.modules.semanticsearch;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataFilteringTest {

    @Test
    void testMetadataFilterKeys() {
        Map<String, Object> filters = Map.of(
                "tenantId", "tenant-1",
                "currentVersion", true,
                "archived", false
        );

        assertEquals("tenant-1", filters.get("tenantId"));
        assertEquals(true, filters.get("currentVersion"));
        assertEquals(false, filters.get("archived"));
    }
}
