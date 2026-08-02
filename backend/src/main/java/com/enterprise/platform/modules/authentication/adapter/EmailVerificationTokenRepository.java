package com.enterprise.platform.modules.authentication.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, UUID> {
    Optional<EmailVerificationTokenEntity> findByTokenValue(UUID tokenValue);
    Optional<EmailVerificationTokenEntity> findByIdentityId(UUID identityId);
    void deleteByIdentityId(UUID identityId);
}
