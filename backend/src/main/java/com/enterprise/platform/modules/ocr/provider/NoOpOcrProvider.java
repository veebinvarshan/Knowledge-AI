package com.enterprise.platform.modules.ocr.provider;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@Component
public class NoOpOcrProvider implements OcrProvider {

    @Override
    public boolean supports(String mimeType) {
        return true; // Fallback supports all formats
    }

    @Override
    public int getPriority() {
        return 100; // Lowest priority
    }

    @Override
    public OcrResult ocr(InputStream inputStream, String language) throws Exception {
        // Drains the stream safely
        byte[] buf = new byte[4096];
        while (inputStream.read(buf) != -1) {}

        return new OcrResult(
                "",
                "unknown",
                0.0,
                List.of(0.0),
                0,
                new HashMap<>(),
                "NoOp",
                new HashMap<>()
        );
    }
}
