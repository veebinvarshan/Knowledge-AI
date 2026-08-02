package com.enterprise.platform.modules.search.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "search_documents", indexes = {
    @Index(name = "idx_search_docs_version", columnList = "version_id"),
    @Index(name = "idx_search_docs_tenant", columnList = "tenant_id")
})
public class SearchDocument {

    @Id
    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "title")
    private String title;

    @Column(name = "filename")
    private String filename;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "language")
    private String language;

    @Column(name = "author")
    private String author;

    @Lob
    @Column(name = "ocr_text")
    private String ocrText;

    @Lob
    @Column(name = "metadata_text")
    private String metadataText;

    @Lob
    @Column(name = "extracted_keywords")
    private String extractedKeywords;

    @Lob
    @Column(name = "normalized_text")
    private String normalizedText;

    @Column(name = "indexed_at", nullable = false)
    private Instant indexedAt;

    @Column(name = "current_version", nullable = false)
    private Boolean currentVersion;

    @Column(name = "permission_hash")
    private String permissionHash;

    @Column(name = "vector_id")
    private UUID vectorId;

    @Column(name = "vector_status")
    private String vectorStatus;

    @Column(name = "embedding_model")
    private String embeddingModel;

    @Column(name = "embedding_version")
    private String embeddingVersion;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "search_metadata", columnDefinition = "jsonb")
    private Map<String, Object> searchMetadata = new HashMap<>();

    public SearchDocument() {}

    private SearchDocument(Builder builder) {
        this.documentId = builder.documentId;
        this.versionId = builder.versionId;
        this.tenantId = builder.tenantId;
        this.title = builder.title;
        this.filename = builder.filename;
        this.mimeType = builder.mimeType;
        this.language = builder.language;
        this.author = builder.author;
        this.ocrText = builder.ocrText;
        this.metadataText = builder.metadataText;
        this.extractedKeywords = builder.extractedKeywords;
        this.normalizedText = builder.normalizedText;
        this.indexedAt = builder.indexedAt != null ? builder.indexedAt : Instant.now();
        this.currentVersion = builder.currentVersion != null ? builder.currentVersion : true;
        this.permissionHash = builder.permissionHash;
        this.vectorId = builder.vectorId;
        this.vectorStatus = builder.vectorStatus != null ? builder.vectorStatus : "NOT_GENERATED";
        this.embeddingModel = builder.embeddingModel;
        this.embeddingVersion = builder.embeddingVersion;
        this.generatedAt = builder.generatedAt;
        this.searchMetadata = builder.searchMetadata != null ? 
                new HashMap<>(builder.searchMetadata) : new HashMap<>();
    }

    public UUID getDocumentId() { return documentId; }
    public UUID getVersionId() { return versionId; }
    public String getTenantId() { return tenantId; }

    public String getTitle() { return title; }
    public String getFilename() { return filename; }
    public String getMimeType() { return mimeType; }
    public String getLanguage() { return language; }
    public String getAuthor() { return author; }

    public String getOcrText() { return ocrText; }
    public String getMetadataText() { return metadataText; }
    public String getExtractedKeywords() { return extractedKeywords; }
    public String getNormalizedText() { return normalizedText; }

    public Instant getIndexedAt() { return indexedAt; }
    public Boolean getCurrentVersion() { return currentVersion; }
    public String getPermissionHash() { return permissionHash; }

    public UUID getVectorId() { return vectorId; }
    public String getVectorStatus() { return vectorStatus; }
    public String getEmbeddingModel() { return embeddingModel; }
    public String getEmbeddingVersion() { return embeddingVersion; }
    public Instant getGeneratedAt() { return generatedAt; }

    public Map<String, Object> getSearchMetadata() {
        return Collections.unmodifiableMap(searchMetadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID documentId;
        private UUID versionId;
        private String tenantId;
        private String title;
        private String filename;
        private String mimeType;
        private String language;
        private String author;
        private String ocrText;
        private String metadataText;
        private String extractedKeywords;
        private String normalizedText;
        private Instant indexedAt;
        private Boolean currentVersion;
        private String permissionHash;
        private UUID vectorId;
        private String vectorStatus;
        private String embeddingModel;
        private String embeddingVersion;
        private Instant generatedAt;
        private Map<String, Object> searchMetadata = new HashMap<>();

        public Builder documentId(UUID val) { this.documentId = val; return this; }
        public Builder versionId(UUID val) { this.versionId = val; return this; }
        public Builder tenantId(String val) { this.tenantId = val; return this; }
        public Builder title(String val) { this.title = val; return this; }
        public Builder filename(String val) { this.filename = val; return this; }
        public Builder mimeType(String val) { this.mimeType = val; return this; }
        public Builder language(String val) { this.language = val; return this; }
        public Builder author(String val) { this.author = val; return this; }
        public Builder ocrText(String val) { this.ocrText = val; return this; }
        public Builder metadataText(String val) { this.metadataText = val; return this; }
        public Builder extractedKeywords(String val) { this.extractedKeywords = val; return this; }
        public Builder normalizedText(String val) { this.normalizedText = val; return this; }
        public Builder indexedAt(Instant val) { this.indexedAt = val; return this; }
        public Builder currentVersion(Boolean val) { this.currentVersion = val; return this; }
        public Builder permissionHash(String val) { this.permissionHash = val; return this; }
        public Builder vectorId(UUID val) { this.vectorId = val; return this; }
        public Builder vectorStatus(String val) { this.vectorStatus = val; return this; }
        public Builder embeddingModel(String val) { this.embeddingModel = val; return this; }
        public Builder embeddingVersion(String val) { this.embeddingVersion = val; return this; }
        public Builder generatedAt(Instant val) { this.generatedAt = val; return this; }
        public Builder searchMetadata(Map<String, Object> val) { this.searchMetadata = val; return this; }

        public SearchDocument build() {
            return new SearchDocument(this);
        }
    }
}
