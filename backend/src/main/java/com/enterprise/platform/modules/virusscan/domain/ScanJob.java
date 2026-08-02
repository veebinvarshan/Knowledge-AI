package com.enterprise.platform.modules.virusscan.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scan_jobs", indexes = {
    @Index(name = "idx_scan_jobs_version", columnList = "version_id"),
    @Index(name = "idx_scan_jobs_status", columnList = "status")
})
public class ScanJob {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "storage_object_id", nullable = false)
    private UUID storageObjectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ScanJobStatus status;

    @Column(name = "engine_name", length = 255)
    private String engineName;

    @Column(name = "engine_version", length = 255)
    private String engineVersion;

    @Column(name = "signature_name", length = 255)
    private String signatureName;

    @Column(name = "scanned_at")
    private Instant scannedAt;

    @Column(name = "scan_duration_ms")
    private Long scanDurationMs;

    @Column(name = "bytes_scanned")
    private Long bytesScanned;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public ScanJob() {}

    public ScanJob(UUID documentId, UUID versionId, UUID storageObjectId) {
        this.documentId = documentId;
        this.versionId = versionId;
        this.storageObjectId = storageObjectId;
        this.status = ScanJobStatus.PENDING;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDocumentId() { return documentId; }
    public UUID getVersionId() { return versionId; }
    public UUID getStorageObjectId() { return storageObjectId; }
    public ScanJobStatus getStatus() { return status; }

    public String getEngineName() { return engineName; }
    public void setEngineName(String engineName) { this.engineName = engineName; }

    public String getEngineVersion() { return engineVersion; }
    public void setEngineVersion(String engineVersion) { this.engineVersion = engineVersion; }

    public String getSignatureName() { return signatureName; }
    public void setSignatureName(String signatureName) { this.signatureName = signatureName; }

    public Instant getScannedAt() { return scannedAt; }
    public void setScannedAt(Instant scannedAt) { this.scannedAt = scannedAt; }

    public Long getScanDurationMs() { return scanDurationMs; }
    public void setScanDurationMs(Long scanDurationMs) { this.scanDurationMs = scanDurationMs; }

    public Long getBytesScanned() { return bytesScanned; }
    public void setBytesScanned(Long bytesScanned) { this.bytesScanned = bytesScanned; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    // State transitions mapping with validations
    public void transitionToScanning() {
        validateTransition(ScanJobStatus.SCANNING);
        this.status = ScanJobStatus.SCANNING;
    }

    public void transitionToClean(String engineName, String engineVersion, long durationMs, long bytes) {
        validateTransition(ScanJobStatus.CLEAN);
        this.status = ScanJobStatus.CLEAN;
        this.engineName = engineName;
        this.engineVersion = engineVersion;
        this.scannedAt = Instant.now();
        this.scanDurationMs = durationMs;
        this.bytesScanned = bytes;
    }

    public void transitionToInfected(String engineName, String engineVersion, String signature, long durationMs, long bytes) {
        validateTransition(ScanJobStatus.INFECTED);
        this.status = ScanJobStatus.INFECTED;
        this.engineName = engineName;
        this.engineVersion = engineVersion;
        this.signatureName = signature;
        this.scannedAt = Instant.now();
        this.scanDurationMs = durationMs;
        this.bytesScanned = bytes;
    }

    public void transitionToQuarantined() {
        validateTransition(ScanJobStatus.QUARANTINED);
        this.status = ScanJobStatus.QUARANTINED;
    }

    public void transitionToFailed() {
        validateTransition(ScanJobStatus.FAILED);
        this.status = ScanJobStatus.FAILED;
    }

    public void transitionToPendingForRetry(Instant nextRetryAt) {
        validateTransition(ScanJobStatus.PENDING);
        this.status = ScanJobStatus.PENDING;
        this.nextRetryAt = nextRetryAt;
        this.retryCount++;
    }

    private void validateTransition(ScanJobStatus target) {
        boolean valid = false;
        switch (this.status) {
            case PENDING -> valid = (target == ScanJobStatus.SCANNING);
            case SCANNING -> valid = (target == ScanJobStatus.CLEAN || target == ScanJobStatus.INFECTED || target == ScanJobStatus.FAILED);
            case INFECTED -> valid = (target == ScanJobStatus.QUARANTINED);
            case FAILED -> valid = (target == ScanJobStatus.PENDING);
            default -> valid = false;
        }
        if (!valid) {
            throw new IllegalStateException("Invalid scan job transition from " + this.status + " to " + target);
        }
    }
}
