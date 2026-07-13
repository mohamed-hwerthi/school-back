package com.schoolSys.schooolSys.analytics;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KpiConfigRepository extends JpaRepository<KpiConfig, UUID> {

    List<KpiConfig> findByActifTrue();

    List<KpiConfig> findByType(String type);
}
