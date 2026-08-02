package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.DuplicateDetectionStrategy;
import com.enterprise.platform.modules.documents.domain.DocumentVersion;
import com.enterprise.platform.modules.documents.repository.DocumentVersionRepository;
import com.enterprise.platform.modules.documents.service.dto.DuplicateDetectionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class DuplicateDetectionServiceImpl implements DuplicateDetectionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DuplicateDetectionStrategy strategy;

    public DuplicateDetectionServiceImpl(
            DocumentVersionRepository documentVersionRepository,
            @Value("${platform.documents.duplicate-strategy:REJECT_DUPLICATE}") String strategyStr) {
        this.documentVersionRepository = documentVersionRepository;
        this.strategy = DuplicateDetectionStrategy.valueOf(strategyStr.toUpperCase());
    }

    @Override
    public DuplicateDetectionResult evaluateDuplicate(String tenantId, String checksum, String fileName, UUID userId) {
        if (strategy == DuplicateDetectionStrategy.ALLOW_DUPLICATE) {
            return new DuplicateDetectionResult(
                    false,
                    strategy,
                    null,
                    null,
                    checksum,
                    "Allow duplicate strategy selected",
                    "PROCEED"
            );
        }

        List<DocumentVersion> duplicates = documentVersionRepository.findActiveVersionsByChecksum(tenantId, checksum);
        if (duplicates.isEmpty()) {
            return new DuplicateDetectionResult(
                    false,
                    strategy,
                    null,
                    null,
                    checksum,
                    "No active duplicate versions found in tenant",
                    "PROCEED"
            );
        }

        DocumentVersion existing = duplicates.get(0);
        UUID existingDocId = existing.getDocument().getId();
        UUID existingVerId = existing.getId();

        String recommendedAction = switch (strategy) {
            case REJECT_DUPLICATE -> "REJECT";
            case CREATE_NEW_REFERENCE -> "REFERENCE";
            case CREATE_NEW_DOCUMENT -> "CREATE_NEW";
            case ALLOW_DUPLICATE -> "PROCEED";
        };

        return new DuplicateDetectionResult(
                true,
                strategy,
                existingDocId,
                existingVerId,
                checksum,
                "Duplicate file content found in tenant (Document ID: " + existingDocId + ")",
                recommendedAction
        );
    }
}
