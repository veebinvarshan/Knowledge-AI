package com.enterprise.platform.modules.metadata.provider;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class ImageMetadataExtractor implements MetadataExtractor {

    private static final Set<String> SUPPORTED_MIMES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp",
            "image/tiff"
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

        Integer width = null;
        String w = metadata.get("tiff:ImageWidth");
        if (w == null) w = metadata.get("Image Width");
        if (w != null) {
            try {
                // Strip suffix " pixels" if present
                w = w.replaceAll("[^0-9]", "");
                width = Integer.parseInt(w);
            } catch (Exception e) {}
        }

        Integer height = null;
        String h = metadata.get("tiff:ImageLength");
        if (h == null) h = metadata.get("Image Height");
        if (h != null) {
            try {
                h = h.replaceAll("[^0-9]", "");
                height = Integer.parseInt(h);
            } catch (Exception e) {}
        }

        Integer dpi = null;
        String res = metadata.get("tiff:XResolution");
        if (res != null) {
            try {
                res = res.replaceAll("[^0-9]", "");
                dpi = Integer.parseInt(res);
            } catch (Exception e) {}
        }

        return new MetadataExtractionResult(
                null, null, null, null, null, null,
                null, null, null,
                width, height, dpi,
                metadata.get("Color Space"),
                metadata.get("tiff:Model"),
                null, null, null, null,
                null, null, null,
                null, null,
                additional
        );
    }
}
