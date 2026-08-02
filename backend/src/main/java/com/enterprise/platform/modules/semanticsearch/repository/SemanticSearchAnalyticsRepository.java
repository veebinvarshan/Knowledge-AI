package com.enterprise.platform.modules.semanticsearch.repository;

import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SemanticSearchAnalyticsRepository extends JpaRepository<SemanticSearchJob, UUID> {}
