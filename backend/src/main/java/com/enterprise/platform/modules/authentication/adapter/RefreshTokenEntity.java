package com.enterprise.platform.modules.authentication.adapter;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", nullable = false)
    private AuthIdentityEntity identity;

    @Column(name = "token_value", nullable = false, unique = true)
    private UUID tokenValue;

    @Column(name = "rotated", nullable = false)
    private boolean rotated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_token_id")
    private RefreshTokenEntity parentToken;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AuthIdentityEntity getIdentity() { return identity; }
    public void setIdentity(AuthIdentityEntity identity) { this.identity = identity; }

    public UUID getTokenValue() { return tokenValue; }
    public void setTokenValue(UUID tokenValue) { this.tokenValue = tokenValue; }

    public boolean isRotated() { return rotated; }
    public void setRotated(boolean rotated) { this.rotated = rotated; }

    public RefreshTokenEntity getParentToken() { return parentToken; }
    public void setParentToken(RefreshTokenEntity parentToken) { this.parentToken = parentToken; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
