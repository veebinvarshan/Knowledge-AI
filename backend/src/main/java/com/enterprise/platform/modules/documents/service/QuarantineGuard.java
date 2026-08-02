package com.enterprise.platform.modules.documents.service;

import java.util.UUID;

public interface QuarantineGuard {
    boolean isQuarantined(UUID versionId);
}
