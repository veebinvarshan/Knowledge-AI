package com.enterprise.platform.modules.authentication.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthAuditEventRepository extends JpaRepository<AuthAuditEventEntity, UUID> {
}
