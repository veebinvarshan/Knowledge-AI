package com.enterprise.platform.modules.virusscan.domain;

public enum ScanJobStatus {
    PENDING,
    SCANNING,
    CLEAN,
    INFECTED,
    FAILED,
    QUARANTINED
}
