package com.enterprise.platform.modules.authentication.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountLockoutRepository extends JpaRepository<AccountLockoutEntity, UUID> {
    Optional<AccountLockoutEntity> findByIdentityIdAndUnlocksAtAfter(UUID identityId, Instant now);
}
