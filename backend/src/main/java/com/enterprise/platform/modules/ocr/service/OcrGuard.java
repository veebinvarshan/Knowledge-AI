package com.enterprise.platform.modules.ocr.service;

import java.util.UUID;

public interface OcrGuard {
    boolean isOcrCompleted(UUID versionId);
}
