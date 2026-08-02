package com.enterprise.platform.modules.virusscan.provider;

import com.enterprise.platform.modules.virusscan.domain.ScanJobStatus;
import java.time.Instant;

public record VirusScanResult(
    ScanJobStatus status,
    String engineName,
    String engineVersion,
    String signatureName,
    Instant scannedAt,
    long scanDurationMs,
    long bytesScanned
) {}
