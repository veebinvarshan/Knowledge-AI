package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import java.util.UUID;

public interface DocumentLifecycleService {
    void archive(Document doc, UUID userId);
    void restore(Document doc, UUID userId);
    void softDelete(Document doc, UUID userId);
}
