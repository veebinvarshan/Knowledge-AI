package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

@Service
public class MetadataServiceImpl implements MetadataService {

    @Override
    public void updateMetadata(Document doc, Map<String, Object> metadata, UUID userId) {
        doc.updateMetadata(metadata, userId);
    }
}
