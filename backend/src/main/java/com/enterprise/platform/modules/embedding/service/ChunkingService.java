package com.enterprise.platform.modules.embedding.service;

import com.enterprise.platform.core.config.properties.ChunkingProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private final ChunkingProperties properties;

    public ChunkingService(ChunkingProperties properties) {
        this.properties = properties;
    }

    public List<Chunk> chunkText(String text, ChunkingStrategy strategy) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        int maxChars = properties.maxCharacters();
        int overlap = properties.overlapCharacters();

        switch (strategy) {
            case FIXED:
                return chunkFixed(text, maxChars, overlap);
            case PARAGRAPH:
                return chunkParagraph(text, maxChars);
            case SENTENCE:
                return chunkSentence(text, maxChars);
            case HYBRID:
                return chunkHybrid(text, maxChars, overlap);
            default:
                return chunkFixed(text, maxChars, overlap);
        }
    }

    private List<Chunk> chunkFixed(String text, int maxChars, int overlap) {
        List<Chunk> chunks = new ArrayList<>();
        int index = 0;
        int step = maxChars - overlap;
        if (step <= 0) step = maxChars;

        while (index < text.length()) {
            int end = Math.min(index + maxChars, text.length());
            String chunkText = text.substring(index, end);
            chunks.add(new Chunk(chunkText, index, end));
            if (end == text.length()) {
                break;
            }
            index += step;
        }
        return chunks;
    }

    private List<Chunk> chunkParagraph(String text, int maxChars) {
        List<Chunk> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");
        int index = 0;

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            int start = text.indexOf(trimmed, index);
            if (start == -1) start = index;
            int end = start + trimmed.length();

            // If a paragraph exceeds maxChars, split it using fixed strategy
            if (trimmed.length() > maxChars) {
                List<Chunk> subChunks = chunkFixed(trimmed, maxChars, 0);
                for (Chunk sub : subChunks) {
                    chunks.add(new Chunk(sub.text(), start + sub.startOffset(), start + sub.endOffset()));
                }
            } else {
                chunks.add(new Chunk(trimmed, start, end));
            }
            index = end;
        }
        return chunks;
    }

    private List<Chunk> chunkSentence(String text, int maxChars) {
        List<Chunk> chunks = new ArrayList<>();
        // Simple sentence boundary check on (.!? )
        String[] sentences = text.split("(?<=[.!?])\\s+");
        int index = 0;

        StringBuilder sb = new StringBuilder();
        int chunkStart = 0;

        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.isEmpty()) continue;

            int sentenceStart = text.indexOf(trimmed, index);
            if (sentenceStart == -1) sentenceStart = index;
            int sentenceEnd = sentenceStart + trimmed.length();

            if (sb.length() + trimmed.length() + 1 > maxChars) {
                if (sb.length() > 0) {
                    chunks.add(new Chunk(sb.toString().trim(), chunkStart, index));
                    sb.setLength(0);
                }
                chunkStart = sentenceStart;
            }

            if (trimmed.length() > maxChars) {
                List<Chunk> subChunks = chunkFixed(trimmed, maxChars, 0);
                for (Chunk sub : subChunks) {
                    chunks.add(new Chunk(sub.text(), sentenceStart + sub.startOffset(), sentenceStart + sub.endOffset()));
                }
                chunkStart = sentenceEnd;
            } else {
                if (sb.length() > 0) sb.append(" ");
                sb.append(trimmed);
            }
            index = sentenceEnd;
        }

        if (sb.length() > 0) {
            chunks.add(new Chunk(sb.toString().trim(), chunkStart, index));
        }

        return chunks;
    }

    private List<Chunk> chunkHybrid(String text, int maxChars, int overlap) {
        List<Chunk> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");
        int index = 0;

        for (String para : paragraphs) {
            String trimmedPara = para.trim();
            if (trimmedPara.isEmpty()) continue;

            int paraStart = text.indexOf(trimmedPara, index);
            if (paraStart == -1) paraStart = index;
            int paraEnd = paraStart + trimmedPara.length();

            if (trimmedPara.length() <= maxChars) {
                chunks.add(new Chunk(trimmedPara, paraStart, paraEnd));
            } else {
                List<Chunk> sentenceChunks = chunkSentence(trimmedPara, maxChars);
                for (Chunk sent : sentenceChunks) {
                    chunks.add(new Chunk(sent.text(), paraStart + sent.startOffset(), paraStart + sent.endOffset()));
                }
            }
            index = paraEnd;
        }
        return chunks;
    }

    public static record Chunk(
        String text,
        int startOffset,
        int endOffset
    ) {}
}
