package com.enterprise.platform.modules.search.indexing;

import org.springframework.stereotype.Component;
import java.text.Normalizer;
import java.util.*;

@Component
public class SearchNormalizer {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", 
            "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", 
            "their", "then", "there", "these", "they", "this", "to", "was", "will", "with"
    );

    public String normalize(String text) {
        if (text == null) return "";
        
        // Unicode Normalization (NFD)
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        
        // Case Folding & Punctuation Clean
        normalized = normalized.toLowerCase()
                .replaceAll("\\p{M}", "") // strip accents
                .replaceAll("[^a-zA-Z0-9\\s]", " ") // replace punctuation with spaces
                .replaceAll("\\s+", " ") // normalize spacing
                .trim();

        // Stemming and Stop-word filtering
        String[] tokens = normalized.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (!STOP_WORDS.contains(token) && token.length() > 1) {
                sb.append(stem(token)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String stem(String word) {
        // Minimal plural/suffix stemmer
        if (word.endsWith("sses")) return word.substring(0, word.length() - 2);
        if (word.endsWith("ies")) return word.substring(0, word.length() - 3) + "i";
        if (word.endsWith("ss")) return word;
        if (word.endsWith("s") && !word.endsWith("us") && !word.endsWith("is") && !word.endsWith("as")) {
            return word.substring(0, word.length() - 1);
        }
        if (word.endsWith("eed")) return word.endsWith("ceed") ? word : word.substring(0, word.length() - 1);
        if (word.endsWith("ing")) return word.substring(0, word.length() - 3);
        if (word.endsWith("ed")) return word.substring(0, word.length() - 2);
        return word;
    }
}
