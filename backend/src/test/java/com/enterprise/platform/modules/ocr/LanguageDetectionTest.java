package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.core.config.properties.OcrProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageDetectionTest {

    @Test
    void testLanguageConfigValue() {
        OcrProperties properties = new OcrProperties(true, "TESSERACT", "eng+deu", 3, 1000, 70.0);
        assertEquals("eng+deu", properties.languages());
        assertEquals("TESSERACT", properties.provider());
        assertTrue(properties.enabled());
    }
}
