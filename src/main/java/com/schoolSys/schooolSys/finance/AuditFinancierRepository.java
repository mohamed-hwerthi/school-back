package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditFinancierRepository extends JpaRepository<AuditFinancier, UUID> {

    List<AuditFinancier> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<AuditFinancier> findByEntityTypeOrderByCreatedAtDesc(String entityType);
}
