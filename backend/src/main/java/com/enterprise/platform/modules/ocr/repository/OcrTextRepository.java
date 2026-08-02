package com.enterprise.platform.modules.ocr.repository;

import com.enterprise.platform.modules.ocr.domain.OcrText;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface OcrTextRepository extends Repository<OcrText, UUID> {
    OcrText save(OcrText text);
    Optional<OcrText> findById(UUID versionId);
    void deleteById(UUID versionId);
}
