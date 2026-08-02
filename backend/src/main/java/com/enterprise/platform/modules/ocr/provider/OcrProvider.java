package com.enterprise.platform.modules.ocr.provider;

import java.io.InputStream;

public interface OcrProvider {
    boolean supports(String mimeType);
    int getPriority(); // Selection priority order (lower is higher priority)
    OcrResult ocr(InputStream inputStream, String language) throws Exception;
}
