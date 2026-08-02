package com.enterprise.platform.modules.search.repository;

import com.enterprise.platform.modules.search.domain.SearchDocument;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface SearchDocumentRepository extends Repository<SearchDocument, UUID> {
    SearchDocument save(SearchDocument doc);
    Optional<SearchDocument> findById(UUID documentId);
    Optional<SearchDocument> findByVersionId(UUID versionId);
    void deleteById(UUID documentId);
    List<SearchDocument> findAllByTenantId(String tenantId);
}
