package com.enterprise.platform.modules.search.service;

import com.enterprise.platform.modules.search.domain.SearchJob;
import java.util.UUID;

public interface SearchIndexService {
    SearchJob submitIndexJob(String tenantId, UUID documentId, UUID versionId, String indexType);
    void executeIndexing(UUID jobId, String tenantId);
}
