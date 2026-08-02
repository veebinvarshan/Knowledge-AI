package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.core.config.properties.TesseractProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StreamingMemoryUsageTest {

    @Test
    void testMaxPagesConstrained() {
        TesseractProperties properties = new TesseractProperties("./tessdata", 10000, 50, 150);
        assertEquals(50, properties.maxPages());
        assertEquals(150, properties.maxImageResolutionDpi());
    }
}
