package com.enterprise.platform.modules.authentication.adapter;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_lockouts")
public class AccountLockoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", nullable = false)
    private AuthIdentityEntity identity;

    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt = Instant.now();

    @Column(name = "unlocks_at", nullable = false)
    private Instant unlocksAt;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason = "MAX_FAILED_ATTEMPTS";

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AuthIdentityEntity getIdentity() { return identity; }
    public void setIdentity(AuthIdentityEntity identity) { this.identity = identity; }

    public Instant getLockedAt() { return lockedAt; }
    public void setLockedAt(Instant lockedAt) { this.lockedAt = lockedAt; }

    public Instant getUnlocksAt() { return unlocksAt; }
    public void setUnlocksAt(Instant unlocksAt) { this.unlocksAt = unlocksAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
