package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import java.util.Map;
import java.util.UUID;

public interface MetadataService {
    void updateMetadata(Document doc, Map<String, Object> metadata, UUID userId);
}
