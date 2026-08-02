package com.enterprise.platform.modules.metadata.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "extracted_metadata")
public class ExtractedMetadata {

    @Id
    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    // Technical Metadata
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "extension")
    private String extension;

    @Column(name = "size")
    private Long size;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "checksum_algorithm")
    private String checksumAlgorithm;

    // Document Metadata
    @Column(name = "title")
    private String title;

    @Column(name = "subject")
    private String subject;

    @Column(name = "author")
    private String author;

    @Column(name = "company")
    private String company;

    @Column(name = "keywords", length = 1000)
    private String keywords;

    @Column(name = "language")
    private String language;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "character_count")
    private Integer characterCount;

    // Image Metadata
    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "dpi")
    private Integer dpi;

    @Column(name = "color_space")
    private String colorSpace;

    @Column(name = "camera_model")
    private String cameraModel;

    // PDF Metadata
    @Column(name = "pdf_version")
    private String pdfVersion;

    @Column(name = "producer")
    private String producer;

    @Column(name = "creator")
    private String creator;

    @Column(name = "encryption_status")
    private String encryptionStatus;

    // Office Metadata
    @Column(name = "application")
    private String application;

    @Column(name = "revision")
    private String revision;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    // Text Metadata
    @Column(name = "encoding")
    private String encoding;

    @Column(name = "line_count")
    private Integer lineCount;

    // Custom Provider Metadata (JSONB column)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_metadata", columnDefinition = "jsonb")
    private Map<String, Object> additionalMetadata = new HashMap<>();

    public ExtractedMetadata() {}

    private ExtractedMetadata(Builder builder) {
        this.versionId = builder.versionId;
        this.documentId = builder.documentId;
        this.tenantId = builder.tenantId;
        this.mimeType = builder.mimeType;
        this.extension = builder.extension;
        this.size = builder.size;
        this.checksum = builder.checksum;
        this.checksumAlgorithm = builder.checksumAlgorithm;
        this.title = builder.title;
        this.subject = builder.subject;
        this.author = builder.author;
        this.company = builder.company;
        this.keywords = builder.keywords;
        this.language = builder.language;
        this.pageCount = builder.pageCount;
        this.wordCount = builder.wordCount;
        this.characterCount = builder.characterCount;
        this.width = builder.width;
        this.height = builder.height;
        this.dpi = builder.dpi;
        this.colorSpace = builder.colorSpace;
        this.cameraModel = builder.cameraModel;
        this.pdfVersion = builder.pdfVersion;
        this.producer = builder.producer;
        this.creator = builder.creator;
        this.encryptionStatus = builder.encryptionStatus;
        this.application = builder.application;
        this.revision = builder.revision;
        this.lastModifiedBy = builder.lastModifiedBy;
        this.encoding = builder.encoding;
        this.lineCount = builder.lineCount;
        this.additionalMetadata = builder.additionalMetadata != null ? 
                new HashMap<>(builder.additionalMetadata) : new HashMap<>();
    }

    public UUID getVersionId() { return versionId; }
    public UUID getDocumentId() { return documentId; }
    public String getTenantId() { return tenantId; }
    
    public String getMimeType() { return mimeType; }
    public String getExtension() { return extension; }
    public Long getSize() { return size; }
    public String getChecksum() { return checksum; }
    public String getChecksumAlgorithm() { return checksumAlgorithm; }

    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getAuthor() { return author; }
    public String getCompany() { return company; }
    public String getKeywords() { return keywords; }
    public String getLanguage() { return language; }
    public Integer getPageCount() { return pageCount; }
    public Integer getWordCount() { return wordCount; }
    public Integer getCharacterCount() { return characterCount; }

    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
    public Integer getDpi() { return dpi; }
    public String getColorSpace() { return colorSpace; }
    public String getCameraModel() { return cameraModel; }

    public String getPdfVersion() { return pdfVersion; }
    public String getProducer() { return producer; }
    public String getCreator() { return creator; }
    public String getEncryptionStatus() { return encryptionStatus; }

    public String getApplication() { return application; }
    public String getRevision() { return revision; }
    public String getLastModifiedBy() { return lastModifiedBy; }

    public String getEncoding() { return encoding; }
    public Integer getLineCount() { return lineCount; }

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
        private String mimeType;
        private String extension;
        private Long size;
        private String checksum;
        private String checksumAlgorithm;
        private String title;
        private String subject;
        private String author;
        private String company;
        private String keywords;
        private String language;
        private Integer pageCount;
        private Integer wordCount;
        private Integer characterCount;
        private Integer width;
        private Integer height;
        private Integer dpi;
        private String colorSpace;
        private String cameraModel;
        private String pdfVersion;
        private String producer;
        private String creator;
        private String encryptionStatus;
        private String application;
        private String revision;
        private String lastModifiedBy;
        private String encoding;
        private Integer lineCount;
        private Map<String, Object> additionalMetadata = new HashMap<>();

        public Builder versionId(UUID val) { this.versionId = val; return this; }
        public Builder documentId(UUID val) { this.documentId = val; return this; }
        public Builder tenantId(String val) { this.tenantId = val; return this; }
        public Builder mimeType(String val) { this.mimeType = val; return this; }
        public Builder extension(String val) { this.extension = val; return this; }
        public Builder size(Long val) { this.size = val; return this; }
        public Builder checksum(String val) { this.checksum = val; return this; }
        public Builder checksumAlgorithm(String val) { this.checksumAlgorithm = val; return this; }
        public Builder title(String val) { this.title = val; return this; }
        public Builder subject(String val) { this.subject = val; return this; }
        public Builder author(String val) { this.author = val; return this; }
        public Builder company(String val) { this.company = val; return this; }
        public Builder keywords(String val) { this.keywords = val; return this; }
        public Builder language(String val) { this.language = val; return this; }
        public Builder pageCount(Integer val) { this.pageCount = val; return this; }
        public Builder wordCount(Integer val) { this.wordCount = val; return this; }
        public Builder characterCount(Integer val) { this.characterCount = val; return this; }
        public Builder width(Integer val) { this.width = val; return this; }
        public Builder height(Integer val) { this.height = val; return this; }
        public Builder dpi(Integer val) { this.dpi = val; return this; }
        public Builder colorSpace(String val) { this.colorSpace = val; return this; }
        public Builder cameraModel(String val) { this.cameraModel = val; return this; }
        public Builder pdfVersion(String val) { this.pdfVersion = val; return this; }
        public Builder producer(String val) { this.producer = val; return this; }
        public Builder creator(String val) { this.creator = val; return this; }
        public Builder encryptionStatus(String val) { this.encryptionStatus = val; return this; }
        public Builder application(String val) { this.application = val; return this; }
        public Builder revision(String val) { this.revision = val; return this; }
        public Builder lastModifiedBy(String val) { this.lastModifiedBy = val; return this; }
        public Builder encoding(String val) { this.encoding = val; return this; }
        public Builder lineCount(Integer val) { this.lineCount = val; return this; }
        public Builder additionalMetadata(Map<String, Object> val) { this.additionalMetadata = val; return this; }

        public ExtractedMetadata build() {
            return new ExtractedMetadata(this);
        }
    }
}
