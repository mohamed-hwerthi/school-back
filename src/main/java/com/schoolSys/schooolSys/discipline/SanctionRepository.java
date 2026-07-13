package com.schoolSys.schooolSys.discipline;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanctionRepository extends JpaRepository<Sanction, UUID> {

    List<Sanction> findByEleveId(UUID eleveId);

    List<Sanction> findByIncidentId(UUID incidentId);

    List<Sanction> findByEleveIdAndStatut(UUID eleveId, String statut);

    List<Sanction> findByEleveIdOrderByCreatedAtDesc(UUID eleveId);
}
