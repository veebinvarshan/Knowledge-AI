package com.enterprise.platform.modules.documents.upload.repository;

import com.enterprise.platform.modules.documents.upload.domain.UploadChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UploadChunkRepository extends JpaRepository<UploadChunk, UUID> {
}
