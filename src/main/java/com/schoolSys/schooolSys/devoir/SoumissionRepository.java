package com.schoolSys.schooolSys.devoir;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoumissionRepository extends JpaRepository<Soumission, UUID> {

    List<Soumission> findByDevoirIdOrderByDateSoumissionDesc(UUID devoirId);

    List<Soumission> findByEleveIdOrderByDateSoumissionDesc(UUID eleveId);

    List<Soumission> findByDevoirIdAndEleveId(UUID devoirId, UUID eleveId);

    long countByDevoirId(UUID devoirId);

    long countByDevoirIdAndCorrigeTrue(UUID devoirId);

    long countByDevoirIdAndEnRetardTrue(UUID devoirId);

    // Year-aware queries
    List<Soumission> findByEleveIdAndAnneeScolaireOrderByDateSoumissionDesc(UUID eleveId, String anneeScolaire);
}
