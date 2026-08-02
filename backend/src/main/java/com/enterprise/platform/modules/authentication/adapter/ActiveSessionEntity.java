package com.enterprise.platform.modules.authentication.adapter;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "active_sessions")
public class ActiveSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", nullable = false)
    private AuthIdentityEntity identity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private AuthDeviceEntity device;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AuthIdentityEntity getIdentity() { return identity; }
    public void setIdentity(AuthIdentityEntity identity) { this.identity = identity; }

    public AuthDeviceEntity getDevice() { return device; }
    public void setDevice(AuthDeviceEntity device) { this.device = device; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(Instant lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
