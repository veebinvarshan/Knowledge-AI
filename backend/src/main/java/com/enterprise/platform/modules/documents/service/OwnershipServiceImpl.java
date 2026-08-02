package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class OwnershipServiceImpl implements OwnershipService {

    @Override
    public void changeOwnership(Document doc, UUID newOwnerId, UUID userId) {
        doc.changeOwner(newOwnerId, userId);
    }
}
