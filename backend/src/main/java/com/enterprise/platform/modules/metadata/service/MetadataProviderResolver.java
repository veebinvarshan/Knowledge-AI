package com.enterprise.platform.modules.metadata.service;

import com.enterprise.platform.modules.metadata.provider.MetadataExtractor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class MetadataProviderResolver {

    private final List<MetadataExtractor> extractors;

    public MetadataProviderResolver(List<MetadataExtractor> extractors) {
        this.extractors = extractors.stream()
                .sorted(Comparator.comparingInt(MetadataExtractor::getPriority))
                .toList();
    }

    public MetadataExtractor resolve(String mimeType) {
        return extractors.stream()
                .filter(e -> e.supports(mimeType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No metadata extractor found for mime type: " + mimeType));
    }

    public List<MetadataExtractor> getExtractors() {
        return extractors;
    }
}
