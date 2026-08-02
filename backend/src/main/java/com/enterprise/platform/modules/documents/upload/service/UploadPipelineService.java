package com.enterprise.platform.modules.documents.upload.service;

import com.enterprise.platform.modules.documents.upload.exception.UploadException;
import com.enterprise.platform.modules.documents.upload.service.dto.*;

import java.io.InputStream;
import java.util.UUID;

public interface UploadPipelineService {

    UploadSessionDto initializeSession(String tenantId, UUID userId, UploadSessionInitDto initDto) throws UploadException;

    ChunkUploadResultDto uploadChunk(UUID sessionId, String tenantId, UUID userId, int chunkNumber, InputStream inputStream, long sizeBytes, String checksum) throws UploadException;

    ResumeStatusDto getResumeStatus(UUID sessionId, String tenantId, UUID userId) throws UploadException;

    UploadSessionDto finalizeUpload(UUID sessionId, String tenantId, UUID userId, String clientChecksum) throws UploadException;

    UploadSessionDto abortUpload(UUID sessionId, String tenantId, UUID userId) throws UploadException;

    void cleanupExpiredSessions();
}
