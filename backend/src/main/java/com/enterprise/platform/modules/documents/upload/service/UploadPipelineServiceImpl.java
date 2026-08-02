package com.enterprise.platform.modules.documents.upload.service;

import com.enterprise.platform.core.config.properties.UploadProperties;
import com.enterprise.platform.modules.documents.upload.domain.*;
import com.enterprise.platform.modules.documents.upload.domain.UploadEvents.*;
import com.enterprise.platform.modules.documents.upload.exception.*;
import com.enterprise.platform.modules.documents.upload.repository.UploadSessionRepository;
import com.enterprise.platform.modules.documents.upload.service.dto.*;
import com.enterprise.platform.modules.storage.domain.StorageObject;
import com.enterprise.platform.modules.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

import com.enterprise.platform.modules.documents.service.DuplicateDetectionService;
import com.enterprise.platform.modules.documents.service.dto.DuplicateDetectionResult;

@Service
@Transactional
public class UploadPipelineServiceImpl implements UploadPipelineService {

    private static final Logger log = LoggerFactory.getLogger(UploadPipelineServiceImpl.class);

    private final UploadSessionRepository sessionRepository;
    private final TemporaryStorageService temporaryStorageService;
    private final StorageService storageService;
    private final QuotaService quotaService;
    private final UploadProperties uploadProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final DuplicateDetectionService duplicateDetectionService;

    @Deprecated
    public UploadPipelineServiceImpl(
            UploadSessionRepository sessionRepository,
            TemporaryStorageService temporaryStorageService,
            StorageService storageService,
            QuotaService quotaService,
            UploadProperties uploadProperties,
            ApplicationEventPublisher eventPublisher) {
        this(sessionRepository, temporaryStorageService, storageService, quotaService, uploadProperties, eventPublisher,
             (tenantId, checksum, fileName, userId) -> new com.enterprise.platform.modules.documents.service.dto.DuplicateDetectionResult(
                     false, com.enterprise.platform.modules.documents.domain.DuplicateDetectionStrategy.ALLOW_DUPLICATE,
                     null, null, checksum, "Bypass duplicate check", "PROCEED"
             ));
    }

    @org.springframework.beans.factory.annotation.Autowired
    public UploadPipelineServiceImpl(
            UploadSessionRepository sessionRepository,
            TemporaryStorageService temporaryStorageService,
            StorageService storageService,
            QuotaService quotaService,
            UploadProperties uploadProperties,
            ApplicationEventPublisher eventPublisher,
            DuplicateDetectionService duplicateDetectionService) {
        this.sessionRepository = sessionRepository;
        this.temporaryStorageService = temporaryStorageService;
        this.storageService = storageService;
        this.quotaService = quotaService;
        this.uploadProperties = uploadProperties;
        this.eventPublisher = eventPublisher;
        this.duplicateDetectionService = duplicateDetectionService;
    }

    @Override
    public UploadSessionDto initializeSession(String tenantId, UUID userId, UploadSessionInitDto initDto) throws UploadException {
        // Validate MIME type is allowed
        validateMimeType(initDto.mimeType());

        // Validate basic file limits
        if (initDto.fileSizeBytes() > uploadProperties.maxFileSize()) {
            throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "File size exceeds limit");
        }
        if (initDto.chunksTotal() > uploadProperties.maxChunksPerUpload()) {
            throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Chunks count exceeds limit");
        }

        // Validate concurrent user/tenant limits
        long userActive = sessionRepository.countByUserIdAndStatusIn(userId, List.of(UploadSessionStatus.INITIALIZED, UploadSessionStatus.UPLOADING));
        if (userActive >= uploadProperties.maxConcurrentUploadsPerUser()) {
            throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Max concurrent uploads per user exceeded");
        }

        long tenantActive = sessionRepository.countByTenantIdAndStatusIn(tenantId, List.of(UploadSessionStatus.INITIALIZED, UploadSessionStatus.UPLOADING));
        if (tenantActive >= uploadProperties.maxConcurrentUploadsPerTenant()) {
            throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Max concurrent uploads per tenant exceeded");
        }

        // Validate quota
        quotaService.validateUploadQuota(tenantId, initDto.fileSizeBytes());

        // Expiry: 1 hour in the future
        Instant expiresAt = Instant.now().plusSeconds(3600);

        UploadSession session = new UploadSession(
                tenantId,
                userId,
                initDto.fileName(),
                initDto.fileSizeBytes(),
                initDto.mimeType(),
                initDto.chunksTotal(),
                expiresAt
        );

        session = sessionRepository.save(session);

        eventPublisher.publishEvent(new UploadSessionInitializedEvent(session.getId(), tenantId, userId));

        return toDto(session);
    }

    @Override
    public ChunkUploadResultDto uploadChunk(UUID sessionId, String tenantId, UUID userId, int chunkNumber,
                                          InputStream inputStream, long sizeBytes, String checksum) throws UploadException {
        UploadSession session = getSessionAndVerifyOwnership(sessionId, tenantId, userId);

        if (session.getStatus() != UploadSessionStatus.INITIALIZED && session.getStatus() != UploadSessionStatus.UPLOADING) {
            throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Upload session is not active");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(UploadSessionStatus.EXPIRED);
            sessionRepository.save(session);
            eventPublisher.publishEvent(new UploadSessionExpiredEvent(sessionId, tenantId, userId));
            throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Upload session expired");
        }

        // Validate chunk size bounds
        if (sizeBytes > uploadProperties.maxChunkSize() || sizeBytes < uploadProperties.minChunkSize()) {
            // Last chunk can be smaller than minChunkSize
            if (chunkNumber != session.getChunksTotal()) {
                throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Invalid chunk size");
            }
        }

        try {
            // Store chunk temporarily
            temporaryStorageService.storeChunk(sessionId, chunkNumber, inputStream, sizeBytes);

            // Read temp chunk to calculate checksum integrity
            TemporaryResource chunkResource = temporaryStorageService.retrieveChunk(sessionId, chunkNumber);
            String calculatedChecksum = calculateSha256(chunkResource.inputStream());
            
            if (!calculatedChecksum.equalsIgnoreCase(checksum)) {
                throw new ChunkOrderInvalidException("Chunk checksum mismatch: calculated " + calculatedChecksum + " but received " + checksum);
            }

            // Register chunk inside aggregate root
            session.addChunk(chunkNumber, sizeBytes, checksum, "SHA256");
            sessionRepository.save(session);

            eventPublisher.publishEvent(new UploadChunkUploadedEvent(sessionId, tenantId, userId, chunkNumber));

            return new ChunkUploadResultDto(sessionId, chunkNumber, true, "Chunk uploaded successfully");
        } catch (IOException e) {
            throw new UploadException("Failed to store chunk temporarily: " + e.getMessage());
        }
    }

    @Override
    public ResumeStatusDto getResumeStatus(UUID sessionId, String tenantId, UUID userId) throws UploadException {
        UploadSession session = getSessionAndVerifyOwnership(sessionId, tenantId, userId);
        List<Integer> uploaded = session.getChunks().stream().map(UploadChunk::getChunkNumber).toList();
        return new ResumeStatusDto(sessionId, session.getStatus(), uploaded, session.getChunksTotal(), session.getFileSizeBytes());
    }

    @Override
    public UploadSessionDto finalizeUpload(UUID sessionId, String tenantId, UUID userId, String clientChecksum) throws UploadException {
        UploadSession session = getSessionAndVerifyOwnership(sessionId, tenantId, userId);

        if (session.getStatus() != UploadSessionStatus.UPLOADING) {
            throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Upload session is not in uploading status");
        }

        // Verify all chunks exist
        if (session.getChunks().size() != session.getChunksTotal()) {
            throw new ChunkOrderInvalidException("Cannot finalize: missing chunks");
        }

        // Sort chunks by number
        List<UploadChunk> sortedChunks = session.getChunks().stream()
                .sorted(Comparator.comparingInt(UploadChunk::getChunkNumber))
                .toList();

        // Create Sequential InputStream
        List<InputStream> streams = new ArrayList<>();
        try {
            for (UploadChunk chunk : sortedChunks) {
                TemporaryResource tempResource = temporaryStorageService.retrieveChunk(sessionId, chunk.getChunkNumber());
                streams.add(tempResource.inputStream());
            }
        } catch (IOException e) {
            throw new UploadException("Failed to retrieve chunks for reassembly: " + e.getMessage());
        }

        InputStream sequentialStream = new SequenceInputStream(Collections.enumeration(streams));
        
        // Calculate SHA-256 during streaming
        String finalChecksum;
        StorageObject storageObject = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(sequentialStream, digest)) {
                // Pipe stream to StorageService
                String logicalPath = tenantId + "/" + userId + "/" + session.getFileName();
                storageObject = storageService.store(dis, logicalPath, session.getMimeType());
            }
            finalChecksum = bytesToHex(digest.digest());
        } catch (Exception e) {
            // Execution rollback compensation
            executeCompensation(sessionId);
            session.setStatus(UploadSessionStatus.FAILED);
            sessionRepository.save(session);
            eventPublisher.publishEvent(new UploadSessionFailedEvent(sessionId, tenantId, userId, "Reassembly failed: " + e.getMessage()));
            throw new UploadException("Reassembly finalization failed: " + e.getMessage());
        }

        // Validate final checksum
        if (clientChecksum != null && !finalChecksum.equalsIgnoreCase(clientChecksum)) {
            executeCompensation(sessionId);
            if (storageObject != null) {
                try {
                    storageService.delete(storageObject.getLogicalPath());
                } catch (Exception ex) {
                    log.error("Failed to clean up storage object on checksum mismatch", ex);
                }
            }
            session.setStatus(UploadSessionStatus.FAILED);
            sessionRepository.save(session);
            eventPublisher.publishEvent(new UploadSessionFailedEvent(sessionId, tenantId, userId, "Final checksum mismatch"));
            throw new UploadException(HttpStatus.BAD_REQUEST, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Final checksum mismatch");
        }

        // Duplicate Check
        DuplicateDetectionResult duplicateResult = duplicateDetectionService.evaluateDuplicate(tenantId, finalChecksum, session.getFileName(), userId);
        if (duplicateResult.duplicateFound() && duplicateResult.recommendedAction().equals("REJECT")) {
            executeCompensation(sessionId);
            if (storageObject != null) {
                try {
                    storageService.delete(storageObject.getLogicalPath());
                } catch (Exception ex) {
                    log.error("Failed to clean up storage object on duplicate rejection", ex);
                }
            }
            session.setStatus(UploadSessionStatus.FAILED);
            sessionRepository.save(session);
            eventPublisher.publishEvent(new UploadSessionFailedEvent(sessionId, tenantId, userId, duplicateResult.reason()));
            throw new com.enterprise.platform.modules.documents.exception.DuplicateDocumentException(duplicateResult.reason());
        }

        // Cleanup temporary storage
        try {
            temporaryStorageService.deleteSessionData(sessionId);
        } catch (IOException e) {
            log.warn("Failed to delete temporary upload chunks for session: {}", sessionId, e);
        }

        session.setStatus(UploadSessionStatus.COMPLETED);
        session = sessionRepository.save(session);

        eventPublisher.publishEvent(new UploadSessionCompletedEvent(sessionId, tenantId, userId, tenantId + "/" + userId + "/" + session.getFileName(), storageObject.getId()));

        return toDto(session);
    }

    @Override
    public UploadSessionDto abortUpload(UUID sessionId, String tenantId, UUID userId) throws UploadException {
        UploadSession session = getSessionAndVerifyOwnership(sessionId, tenantId, userId);
        try {
            temporaryStorageService.deleteSessionData(sessionId);
        } catch (IOException e) {
            log.error("Failed to delete aborted temporary session files", e);
        }

        session.setStatus(UploadSessionStatus.ABORTED);
        session = sessionRepository.save(session);

        eventPublisher.publishEvent(new UploadSessionAbortedEvent(sessionId, tenantId, userId));

        return toDto(session);
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredSessions() {
        log.info("Running expired upload sessions cleanup routine...");
        List<UploadSession> expired = sessionRepository.findAllByStatusInAndExpiresAtBefore(
                List.of(UploadSessionStatus.INITIALIZED, UploadSessionStatus.UPLOADING),
                Instant.now()
        );

        for (UploadSession session : expired) {
            try {
                temporaryStorageService.deleteSessionData(session.getId());
                session.setStatus(UploadSessionStatus.EXPIRED);
                sessionRepository.save(session);
                eventPublisher.publishEvent(new UploadSessionExpiredEvent(session.getId(), session.getTenantId(), session.getUserId()));
                log.info("Expired and cleaned upload session: {}", session.getId());
            } catch (Exception e) {
                log.error("Failed to cleanup expired session: {}", session.getId(), e);
            }
        }
    }

    private UploadSession getSessionAndVerifyOwnership(UUID sessionId, String tenantId, UUID userId) throws UploadException {
        UploadSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new UploadException(HttpStatus.NOT_FOUND, com.enterprise.platform.core.exception.ErrorCode.VALIDATION_FAILED, "Upload session not found"));

        if (!session.getTenantId().equals(tenantId) || !session.getUserId().equals(userId)) {
            throw new UploadSessionOwnershipException("Access denied: invalid upload session ownership");
        }
        return session;
    }

    private void executeCompensation(UUID sessionId) {
        try {
            temporaryStorageService.deleteSessionData(sessionId);
        } catch (IOException e) {
            log.error("Compensation failed to delete temporary directories: {}", sessionId, e);
        }
    }

    private void validateMimeType(String mimeType) {
        List<String> whitelist = List.of(
                "application/pdf", "image/png", "image/jpeg", "text/plain", "application/json", "application/octet-stream"
        );
        if (mimeType == null || !whitelist.contains(mimeType.toLowerCase())) {
            throw new UnsupportedMimeTypeException("Unsupported upload format: " + mimeType);
        }
    }

    private String calculateSha256(InputStream in) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            throw new IOException("Failed to calculate SHA-256 checksum", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private UploadSessionDto toDto(UploadSession s) {
        return new UploadSessionDto(
                s.getId(), s.getFileName(), s.getFileSizeBytes(), s.getMimeType(), s.getStatus(), s.getChunksTotal(), s.getExpiresAt(), s.getCreatedAt()
        );
    }
}
