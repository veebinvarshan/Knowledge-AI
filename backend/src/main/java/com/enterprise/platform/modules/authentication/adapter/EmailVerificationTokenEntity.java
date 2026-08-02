package com.enterprise.platform.modules.authentication.adapter;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", nullable = false, unique = true)
    private AuthIdentityEntity identity;

    @Column(name = "token_value", nullable = false, unique = true)
    private UUID tokenValue;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AuthIdentityEntity getIdentity() { return identity; }
    public void setIdentity(AuthIdentityEntity identity) { this.identity = identity; }

    public UUID getTokenValue() { return tokenValue; }
    public void setTokenValue(UUID tokenValue) { this.tokenValue = tokenValue; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
