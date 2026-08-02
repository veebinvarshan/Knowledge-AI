package com.enterprise.platform.modules.metadata.provider;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class TextMetadataExtractor implements MetadataExtractor {

    private static final Set<String> SUPPORTED_MIMES = Set.of(
            "text/plain",
            "text/csv",
            "text/html",
            "text/xml",
            "application/json",
            "application/xml"
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
        // Wrap input stream to read line-by-line (constant memory) and count lines & characters
        // We make a copy of the stream by writing/reading if needed? No, we shouldn't buffer the stream in memory!
        // Wait, Tika's TXTParser needs to parse it, and we also want to read it.
        // If we read it line-by-line first, we can count lines, character count, and words.
        // But then the stream is consumed!
        // Can we use Tika's TXTParser on the stream, and get character counts from Tika's parsed handler?
        // Yes! BodyContentHandler collects the text. If we use body content length, that is character count.
        // To prevent loading the entire text in memory, Tika's WriteOutMDC handler or a custom ContentHandler can count lines and characters as Tika SAX events fire!
        // That is an elegant and highly optimized constant-memory streaming SAX handler!
        // Let's write a custom SAX ContentHandler that counts characters, words, and lines on the fly!
        
        Metadata metadata = new Metadata();
        TXTParser parser = new TXTParser();
        
        // Custom SAX handler to count lines, words, characters on the fly without storing content in memory
        LineCountingHandler handler = new LineCountingHandler();
        
        parser.parse(inputStream, handler, metadata, new org.apache.tika.parser.ParseContext());

        Map<String, Object> additional = new HashMap<>();
        for (String name : metadata.names()) {
            additional.put(name, metadata.get(name));
        }

        return new MetadataExtractionResult(
                null, null, null, null, null, null,
                null, handler.getWordCount(), handler.getCharCount(),
                null, null, null, null, null,
                null, null, null, null,
                null, null, null,
                metadata.get("Content-Encoding"),
                handler.getLineCount(),
                additional
        );
    }

    private static class LineCountingHandler extends org.xml.sax.helpers.DefaultHandler {
        private int lineCount = 0;
        private int charCount = 0;
        private int wordCount = 0;
        private boolean lastWasWhitespace = true;

        @Override
        public void characters(char[] ch, int start, int length) {
            charCount += length;
            for (int i = start; i < start + length; i++) {
                char c = ch[i];
                if (c == '\n') {
                    lineCount++;
                }
                
                if (Character.isWhitespace(c)) {
                    lastWasWhitespace = true;
                } else {
                    if (lastWasWhitespace) {
                        wordCount++;
                    }
                    lastWasWhitespace = false;
                }
            }
        }

        public int getLineCount() {
            // Count the last line if it didn't end with a newline
            return lineCount > 0 || charCount == 0 ? lineCount : lineCount + 1;
        }

        public int getCharCount() { return charCount; }
        public int getWordCount() { return wordCount; }
    }
}
