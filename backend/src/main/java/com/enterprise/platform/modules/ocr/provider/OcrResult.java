package com.enterprise.platform.modules.ocr.provider;

import java.util.List;
import java.util.Map;

public record OcrResult(
    String extractedText,
    String language,
    double confidenceScore,
    List<Double> perPageConfidence,
    int pageCount,
    Map<String, Object> pageBoundaries,
    String provider,
    Map<String, Object> additionalMetadata
) {}
