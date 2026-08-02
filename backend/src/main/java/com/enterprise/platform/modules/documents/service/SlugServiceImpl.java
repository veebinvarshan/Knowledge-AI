package com.enterprise.platform.modules.documents.service;

import com.enterprise.platform.modules.documents.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SlugServiceImpl implements SlugService {

    private static final Pattern NON_ALPHANUM = Pattern.compile("[^a-z0-9-]");
    private static final Pattern MULTI_DASH = Pattern.compile("-+");

    private final DocumentRepository documentRepository;

    public SlugServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public String generateUniqueSlug(String title, String tenantId, UUID docId) {
        String baseSlug = slugify(title);
        String candidate = baseSlug;
        int counter = 1;

        while (true) {
            boolean exists;
            if (docId == null) {
                exists = documentRepository.existsByTenantIdAndSlugAndDeletedAtIsNull(tenantId, candidate);
            } else {
                exists = documentRepository.existsByTenantIdAndSlugAndDeletedAtIsNullAndIdNot(tenantId, candidate, docId);
            }

            if (!exists) {
                return candidate;
            }
            candidate = baseSlug + "-" + (counter++);
        }
    }

    private String slugify(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "document";
        }
        String clean = title.toLowerCase().trim();
        clean = NON_ALPHANUM.matcher(clean).replaceAll("-");
        clean = MULTI_DASH.matcher(clean).replaceAll("-");
        if (clean.startsWith("-")) {
            clean = clean.substring(1);
        }
        if (clean.endsWith("-")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean.isEmpty() ? "document" : clean;
    }
}
