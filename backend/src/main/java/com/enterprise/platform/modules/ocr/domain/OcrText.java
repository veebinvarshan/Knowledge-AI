package com.enterprise.platform.modules.ocr.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ocr_texts")
public class OcrText {

    @Id
    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "language")
    private String language;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "page_count")
    private Integer pageCount;

    @Lob
    @Column(name = "extracted_text")
    private String extractedText;

    @Column(name = "extracted_at", nullable = false)
    private Instant extractedAt;

    @Column(name = "provider")
    private String provider;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "page_boundaries", columnDefinition = "jsonb")
    private Map<String, Object> pageBoundaries = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_metadata", columnDefinition = "jsonb")
    private Map<String, Object> additionalMetadata = new HashMap<>();

    public OcrText() {}

    private OcrText(Builder builder) {
        this.versionId = builder.versionId;
        this.documentId = builder.documentId;
        this.tenantId = builder.tenantId;
        this.language = builder.language;
        this.confidenceScore = builder.confidenceScore;
        this.pageCount = builder.pageCount;
        this.extractedText = builder.extractedText;
        this.extractedAt = builder.extractedAt != null ? builder.extractedAt : Instant.now();
        this.provider = builder.provider;
        this.pageBoundaries = builder.pageBoundaries != null ? 
                new HashMap<>(builder.pageBoundaries) : new HashMap<>();
        this.additionalMetadata = builder.additionalMetadata != null ? 
                new HashMap<>(builder.additionalMetadata) : new HashMap<>();
    }

    public UUID getVersionId() { return versionId; }
    public UUID getDocumentId() { return documentId; }
    public String getTenantId() { return tenantId; }

    public String getLanguage() { return language; }
    public Double getConfidenceScore() { return confidenceScore; }
    public Integer getPageCount() { return pageCount; }
    public String getExtractedText() { return extractedText; }
    public Instant getExtractedAt() { return extractedAt; }
    public String getProvider() { return provider; }

    public Map<String, Object> getPageBoundaries() {
        return Collections.unmodifiableMap(pageBoundaries);
    }

    public Map<String, Object> getAdditionalMetadata() {
        return Collections.unmodifiableMap(additionalMetadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID versionId;
        private UUID documentId;
        private String tenantId;
        private String language;
        private Double confidenceScore;
        private Integer pageCount;
        private String extractedText;
        private Instant extractedAt;
        private String provider;
        private Map<String, Object> pageBoundaries = new HashMap<>();
        private Map<String, Object> additionalMetadata = new HashMap<>();

        public Builder versionId(UUID val) { this.versionId = val; return this; }
        public Builder documentId(UUID val) { this.documentId = val; return this; }
        public Builder tenantId(String val) { this.tenantId = val; return this; }
        public Builder language(String val) { this.language = val; return this; }
        public Builder confidenceScore(Double val) { this.confidenceScore = val; return this; }
        public Builder pageCount(Integer val) { this.pageCount = val; return this; }
        public Builder extractedText(String val) { this.extractedText = val; return this; }
        public Builder extractedAt(Instant val) { this.extractedAt = val; return this; }
        public Builder provider(String val) { this.provider = val; return this; }
        public Builder pageBoundaries(Map<String, Object> val) { this.pageBoundaries = val; return this; }
        public Builder additionalMetadata(Map<String, Object> val) { this.additionalMetadata = val; return this; }

        public OcrText build() {
            return new OcrText(this);
        }
    }
}
