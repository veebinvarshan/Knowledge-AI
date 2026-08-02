package com.enterprise.platform.modules.ocr.service;

import com.enterprise.platform.modules.ocr.provider.OcrProvider;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class OcrProviderResolver {

    private final List<OcrProvider> providers;

    public OcrProviderResolver(List<OcrProvider> providers) {
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(OcrProvider::getPriority))
                .toList();
    }

    public OcrProvider resolve(String mimeType) {
        return providers.stream()
                .filter(p -> p.supports(mimeType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No OCR provider found for mime type: " + mimeType));
    }

    public List<OcrProvider> getProviders() {
        return providers;
    }
}
