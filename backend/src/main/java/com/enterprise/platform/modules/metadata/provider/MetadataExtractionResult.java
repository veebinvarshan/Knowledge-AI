package com.enterprise.platform.modules.metadata.provider;

import java.util.Map;

public record MetadataExtractionResult(
    String title,
    String subject,
    String author,
    String company,
    String keywords,
    String language,
    Integer pageCount,
    Integer wordCount,
    Integer characterCount,
    
    // Image specifics
    Integer width,
    Integer height,
    Integer dpi,
    String colorSpace,
    String cameraModel,
    
    // PDF specifics
    String pdfVersion,
    String producer,
    String creator,
    String encryptionStatus,
    
    // Office specifics
    String application,
    String revision,
    String lastModifiedBy,
    
    // Text specifics
    String encoding,
    Integer lineCount,
    
    // Additional properties for JSONB column
    Map<String, Object> additionalMetadata
) {}
