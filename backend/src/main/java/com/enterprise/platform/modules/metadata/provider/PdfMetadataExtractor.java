package com.enterprise.platform.modules.metadata.provider;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class PdfMetadataExtractor implements MetadataExtractor {

    @Override
    public boolean supports(String mimeType) {
        return "application/pdf".equalsIgnoreCase(mimeType);
    }

    @Override
    public int getPriority() {
        return 1; // High priority (MIME-specific)
    }

    @Override
    public MetadataExtractionResult extract(InputStream inputStream) throws Exception {
        Metadata metadata = new Metadata();
        PDFParser parser = new PDFParser();
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
            pages = metadata.get("pdf:pageCount");
        }
        if (pages != null) {
            try {
                pageCount = Integer.parseInt(pages);
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        return new MetadataExtractionResult(
                metadata.get(TikaCoreProperties.TITLE),
                metadata.get(TikaCoreProperties.DESCRIPTION),
                metadata.get(TikaCoreProperties.CREATOR),
                metadata.get("Company"),
                metadata.get("keywords"),
                metadata.get(TikaCoreProperties.LANGUAGE),
                pageCount, null, null,
                null, null, null, null, null,
                metadata.get("pdf:PDFVersion"),
                metadata.get("pdf:producer"),
                metadata.get("pdf:creator"),
                metadata.get("pdf:encrypted"),
                null, null, null,
                null, null,
                additional
        );
    }
}
