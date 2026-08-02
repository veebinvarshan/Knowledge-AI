package com.enterprise.platform.modules.authentication.adapter;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_devices", uniqueConstraints = {
    @UniqueConstraint(name = "uq_auth_devices_identity_fingerprint", columnNames = {"identity_id", "fingerprint_hash"})
})
public class AuthDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", nullable = false)
    private AuthIdentityEntity identity;

    @Column(name = "fingerprint_hash", nullable = false, length = 64)
    private String fingerprintHash;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName = "Unknown Device";

    @Column(name = "last_ip_address", nullable = false, length = 45)
    private String lastIpAddress;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt = Instant.now();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AuthIdentityEntity getIdentity() { return identity; }
    public void setIdentity(AuthIdentityEntity identity) { this.identity = identity; }

    public String getFingerprintHash() { return fingerprintHash; }
    public void setFingerprintHash(String fingerprintHash) { this.fingerprintHash = fingerprintHash; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getLastIpAddress() { return lastIpAddress; }
    public void setLastIpAddress(String lastIpAddress) { this.lastIpAddress = lastIpAddress; }

    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}
