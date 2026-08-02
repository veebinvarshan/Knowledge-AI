package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.domain.Document;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class DocumentLifecycleServiceImpl implements DocumentLifecycleService {

    @Override
    public void archive(Document doc, UUID userId) {
        doc.archive(userId);
    }

    @Override
    public void restore(Document doc, UUID userId) {
        doc.restore(userId);
    }

    @Override
    public void softDelete(Document doc, UUID userId) {
        doc.softDelete(userId);
    }
}
