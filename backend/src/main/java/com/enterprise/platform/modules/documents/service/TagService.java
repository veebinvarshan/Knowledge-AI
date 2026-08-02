package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import java.util.Set;
import java.util.UUID;

public interface TagService {
    void assignTags(Document doc, Set<UUID> tagIds, UUID userId);
    void removeTags(Document doc, Set<UUID> tagIds, UUID userId);
}
