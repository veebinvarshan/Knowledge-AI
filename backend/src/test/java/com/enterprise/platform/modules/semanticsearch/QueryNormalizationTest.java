package com.enterprise.platform.modules.semanticsearch;

import org.junit.jupiter.api.Test;

import java.text.Normalizer;

import static org.junit.jupiter.api.Assertions.*;

public class QueryNormalizationTest {

    @Test
    void testQueryNormalizesCorrectly() {
        String raw = "Am\u00e9lie's Quick brown fox!!!   ";
        // Form NFD decomposed representation
        String decomposed = Normalizer.normalize(raw, Normalizer.Form.NFD);
        String cleaned = decomposed.replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[\\p{Punct}]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // THEN: accents split, punctuation removed, lowercase normalized spacing
        assertEquals("amelie s quick brown fox", cleaned);
    }
}
