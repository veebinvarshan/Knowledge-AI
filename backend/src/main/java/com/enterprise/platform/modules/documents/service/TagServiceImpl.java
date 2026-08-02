package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.UUID;

@Service
public class TagServiceImpl implements TagService {

    @Override
    public void assignTags(Document doc, Set<UUID> tagIds, UUID userId) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("Tag IDs set cannot be empty");
        }
        // Business validation (e.g. max tags count check)
        if (tagIds.size() > 50) {
            throw new IllegalArgumentException("Cannot assign more than 50 tags to a single document");
        }
    }

    @Override
    public void removeTags(Document doc, Set<UUID> tagIds, UUID userId) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("Tag IDs set cannot be empty");
        }
    }
}
