package com.enterprise.platform.modules.embedding.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "embedding_chunks")
public class EmbeddingChunk {

    @Id
    @Column(name = "chunk_id", nullable = false)
    private UUID chunkId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "token_count", nullable = false)
    private Integer tokenCount;

    @Column(name = "character_count", nullable = false)
    private Integer characterCount;

    @Column(name = "start_offset", nullable = false)
    private Integer startOffset;

    @Column(name = "end_offset", nullable = false)
    private Integer endOffset;

    @Column(name = "text_hash", nullable = false)
    private String textHash;

    @Column(name = "vector_id", nullable = false)
    private UUID vectorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Refinement 4 extra fields
    @Column(name = "chunk_hash", nullable = false)
    private String chunkHash;

    @Column(name = "source_version", nullable = false)
    private UUID sourceVersion;

    @Column(name = "source_checksum", nullable = false)
    private String sourceChecksum;

    @Column(name = "source_length", nullable = false)
    private Integer sourceLength;

    @Column(name = "embedding_model", nullable = false)
    private String embeddingModel;

    @Column(name = "embedding_model_version", nullable = false)
    private String embeddingModelVersion;

    @Column(name = "chunk_text", nullable = false, length = 4000)
    private String chunkText;

    protected EmbeddingChunk() {}

    private EmbeddingChunk(Builder builder) {
        this.chunkId = UUID.randomUUID();
        this.documentId = builder.documentId;
        this.versionId = builder.versionId;
        this.chunkIndex = builder.chunkIndex;
        this.tokenCount = builder.tokenCount;
        this.characterCount = builder.characterCount;
        this.startOffset = builder.startOffset;
        this.endOffset = builder.endOffset;
        this.textHash = builder.textHash;
        this.vectorId = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.chunkHash = builder.chunkHash;
        this.sourceVersion = builder.sourceVersion;
        this.sourceChecksum = builder.sourceChecksum;
        this.sourceLength = builder.sourceLength;
        this.embeddingModel = builder.embeddingModel;
        this.embeddingModelVersion = builder.embeddingModelVersion;
        this.chunkText = builder.chunkText;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getChunkId() { return chunkId; }
    public UUID getDocumentId() { return documentId; }
    public UUID getVersionId() { return versionId; }
    public Integer getChunkIndex() { return chunkIndex; }
    public Integer getTokenCount() { return tokenCount; }
    public Integer getCharacterCount() { return characterCount; }
    public Integer getStartOffset() { return startOffset; }
    public Integer getEndOffset() { return endOffset; }
    public String getTextHash() { return textHash; }
    public UUID getVectorId() { return vectorId; }
    public Instant getCreatedAt() { return createdAt; }
    public String getChunkHash() { return chunkHash; }
    public UUID getSourceVersion() { return sourceVersion; }
    public String getSourceChecksum() { return sourceChecksum; }
    public Integer getSourceLength() { return sourceLength; }
    public String getEmbeddingModel() { return embeddingModel; }
    public String getEmbeddingModelVersion() { return embeddingModelVersion; }
    public String getChunkText() { return chunkText; }

    public static class Builder {
        private UUID documentId;
        private UUID versionId;
        private Integer chunkIndex;
        private Integer tokenCount;
        private Integer characterCount;
        private Integer startOffset;
        private Integer endOffset;
        private String textHash;
        private String chunkHash;
        private UUID sourceVersion;
        private String sourceChecksum;
        private Integer sourceLength;
        private String embeddingModel;
        private String embeddingModelVersion;
        private String chunkText;

        public Builder documentId(UUID documentId) { this.documentId = documentId; return this; }
        public Builder versionId(UUID versionId) { this.versionId = versionId; return this; }
        public Builder chunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; return this; }
        public Builder tokenCount(Integer tokenCount) { this.tokenCount = tokenCount; return this; }
        public Builder characterCount(Integer characterCount) { this.characterCount = characterCount; return this; }
        public Builder startOffset(Integer startOffset) { this.startOffset = startOffset; return this; }
        public Builder endOffset(Integer endOffset) { this.endOffset = endOffset; return this; }
        public Builder textHash(String textHash) { this.textHash = textHash; return this; }
        public Builder chunkHash(String chunkHash) { this.chunkHash = chunkHash; return this; }
        public Builder sourceVersion(UUID sourceVersion) { this.sourceVersion = sourceVersion; return this; }
        public Builder sourceChecksum(String sourceChecksum) { this.sourceChecksum = sourceChecksum; return this; }
        public Builder sourceLength(Integer sourceLength) { this.sourceLength = sourceLength; return this; }
        public Builder embeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; return this; }
        public Builder embeddingModelVersion(String embeddingModelVersion) { this.embeddingModelVersion = embeddingModelVersion; return this; }
        public Builder chunkText(String chunkText) { this.chunkText = chunkText; return this; }

        public EmbeddingChunk build() {
            return new EmbeddingChunk(this);
        }
    }
}
