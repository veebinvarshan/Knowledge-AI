package com.enterprise.platform.modules.metadata.repository;

import com.enterprise.platform.modules.metadata.domain.ExtractedMetadata;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface ExtractedMetadataRepository extends Repository<ExtractedMetadata, UUID> {
    ExtractedMetadata save(ExtractedMetadata metadata);
    Optional<ExtractedMetadata> findById(UUID versionId);
    void deleteById(UUID versionId);
}
