package com.enterprise.platform.modules.rag.repository;

import com.enterprise.platform.modules.rag.domain.RagJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RagAnalyticsRepository extends JpaRepository<RagJob, UUID> {}
