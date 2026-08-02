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
import java.util.Set;

@Component
public class OfficeMetadataExtractor implements MetadataExtractor {

    private static final Set<String> SUPPORTED_MIMES = Set.of(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    @Override
    public boolean supports(String mimeType) {
        if (mimeType == null) return false;
        return SUPPORTED_MIMES.contains(mimeType.toLowerCase());
    }

    @Override
    public int getPriority() {
        return 1; // MIME-specific
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

        Integer pageCount = null;
        String pages = metadata.get("xmpTPg:NPages");
        if (pages == null) {
            pages = metadata.get("meta:page-count");
        }
        if (pages != null) {
            try { pageCount = Integer.parseInt(pages); } catch (Exception e) {}
        }

        Integer wordCount = null;
        String words = metadata.get("meta:word-count");
        if (words != null) {
            try { wordCount = Integer.parseInt(words); } catch (Exception e) {}
        }

        Integer charCount = null;
        String chars = metadata.get("meta:character-count");
        if (chars != null) {
            try { charCount = Integer.parseInt(chars); } catch (Exception e) {}
        }

        return new MetadataExtractionResult(
                metadata.get(TikaCoreProperties.TITLE),
                metadata.get(TikaCoreProperties.DESCRIPTION),
                metadata.get(TikaCoreProperties.CREATOR),
                metadata.get("Company"),
                metadata.get("keywords"),
                metadata.get(TikaCoreProperties.LANGUAGE),
                pageCount, wordCount, charCount,
                null, null, null, null, null,
                null, null, null, null,
                metadata.get("Application"),
                metadata.get("custom:RevisionNumber"),
                metadata.get("Last-Modified"),
                null, null,
                additional
        );
    }
}
