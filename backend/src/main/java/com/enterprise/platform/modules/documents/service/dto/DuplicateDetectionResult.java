package com.enterprise.platform.modules.documents.service.dto;

import com.enterprise.platform.modules.documents.domain.DuplicateDetectionStrategy;
import java.util.UUID;

public record DuplicateDetectionResult(
    boolean duplicateFound,
    DuplicateDetectionStrategy selectedStrategy,
    UUID existingDocumentId,
    UUID existingVersionId,
    String checksum,
    String reason,
    String recommendedAction
) {}
