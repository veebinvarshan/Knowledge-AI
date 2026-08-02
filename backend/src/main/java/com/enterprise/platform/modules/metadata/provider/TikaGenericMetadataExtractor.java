package com.enterprise.platform.modules.metadata.provider;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class TikaGenericMetadataExtractor implements MetadataExtractor {

    @Override
    public boolean supports(String mimeType) {
        return true; // Generic parser fallback for Tika
    }

    @Override
    public int getPriority() {
        return 2; // Priority 2 (Generic Apache Tika extractor)
    }

    @Override
    public MetadataExtractionResult extract(InputStream inputStream) throws Exception {
        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        ParseContext context = new ParseContext();

        parser.parse(inputStream, handler, metadata, context);

        Map<String, Object> additional = new HashMap<>();
        for (String name : metadata.names()) {
            additional.put(name, metadata.get(name));
        }

        return new MetadataExtractionResult(
                metadata.get(TikaCoreProperties.TITLE),
                metadata.get(TikaCoreProperties.DESCRIPTION),
                metadata.get(TikaCoreProperties.CREATOR),
                metadata.get("Company"),
                metadata.get("keywords"),
                metadata.get(TikaCoreProperties.LANGUAGE),
                null, null, null,
                null, null, null, null, null,
                null, null, null, null,
                null, null, null,
                metadata.get("Content-Encoding"),
                null,
                additional
        );
    }
}
