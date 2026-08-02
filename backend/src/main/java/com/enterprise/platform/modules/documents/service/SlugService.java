package com.enterprise.platform.modules.documents.service;

import java.util.UUID;

public interface SlugService {
    String generateUniqueSlug(String title, String tenantId, UUID docId);
}
