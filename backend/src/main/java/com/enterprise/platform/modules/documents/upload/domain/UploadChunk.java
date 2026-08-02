package com.enterprise.platform.modules.documents.upload.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "upload_chunks", indexes = {
    @Index(name = "idx_upload_chunks_session", columnList = "session_id, chunk_number")
})
public class UploadChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private UploadSession session;

    @Column(name = "chunk_number", nullable = false)
    private Integer chunkNumber;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(nullable = false, length = 255)
    private String checksum;

    @Column(name = "checksum_algorithm", nullable = false, length = 50)
    private String checksumAlgorithm;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    public UploadChunk() {}

    public UploadChunk(UploadSession session, Integer chunkNumber, Long sizeBytes, String checksum, String checksumAlgorithm) {
        this.session = session;
        this.chunkNumber = chunkNumber;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.checksumAlgorithm = checksumAlgorithm;
        this.uploadedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UploadSession getSession() { return session; }
    public void setSession(UploadSession session) { this.session = session; }

    public Integer getChunkNumber() { return chunkNumber; }
    public void setChunkNumber(Integer chunkNumber) { this.chunkNumber = chunkNumber; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getChecksumAlgorithm() { return checksumAlgorithm; }
    public void setChecksumAlgorithm(String checksumAlgorithm) { this.checksumAlgorithm = checksumAlgorithm; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
