package com.enterprise.platform.modules.semanticsearch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimilarityMetricTest {

    @Test
    void testCosineSimilarityCalculation() {
        float[] vectorA = {1.0f, 0.0f, 1.0f};
        float[] vectorB = {1.0f, 1.0f, 0.0f};

        // Cosine similarity = dot_product(A, B) / (norm(A)*norm(B))
        // dot_product = 1*1 + 0*1 + 1*0 = 1
        // norm(A) = sqrt(2)
        // norm(B) = sqrt(2)
        // Cosine similarity = 1 / 2 = 0.5
        float dotProduct = 0f;
        float normA = 0f;
        float normB = 0f;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        assertEquals(0.5, similarity, 1e-6);
    }
}
