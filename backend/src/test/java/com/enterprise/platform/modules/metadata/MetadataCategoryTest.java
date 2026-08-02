package com.enterprise.platform.modules.metadata;

import com.enterprise.platform.modules.metadata.domain.MetadataCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataCategoryTest {

    @Test
    void testCategoriesExist() {
        MetadataCategory[] categories = MetadataCategory.values();
        assertTrue(categories.length >= 7);
        assertEquals(MetadataCategory.TECHNICAL, MetadataCategory.valueOf("TECHNICAL"));
        assertEquals(MetadataCategory.DOCUMENT, MetadataCategory.valueOf("DOCUMENT"));
        assertEquals(MetadataCategory.IMAGE, MetadataCategory.valueOf("IMAGE"));
        assertEquals(MetadataCategory.PDF, MetadataCategory.valueOf("PDF"));
        assertEquals(MetadataCategory.OFFICE, MetadataCategory.valueOf("OFFICE"));
        assertEquals(MetadataCategory.TEXT, MetadataCategory.valueOf("TEXT"));
        assertEquals(MetadataCategory.CUSTOM, MetadataCategory.valueOf("CUSTOM"));
    }
}
