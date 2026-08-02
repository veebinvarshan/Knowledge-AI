package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import java.util.UUID;

public interface OwnershipService {
    void changeOwnership(Document doc, UUID newOwnerId, UUID userId);
}
