package com.enterprise.platform.modules.authentication.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthCredentialsRepository extends JpaRepository<AuthCredentialsEntity, UUID> {
    Optional<AuthCredentialsEntity> findByIdentityId(UUID identityId);
}
