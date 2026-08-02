package com.enterprise.platform.modules.authentication.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthIdentityLinkRepository extends JpaRepository<OAuthIdentityLinkEntity, UUID> {
    Optional<OAuthIdentityLinkEntity> findByProviderNameAndProviderUserId(String providerName, String providerUserId);
}
