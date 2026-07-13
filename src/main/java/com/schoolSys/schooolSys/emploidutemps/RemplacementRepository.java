package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface RemplacementRepository extends JpaRepository<Remplacement, UUID> {
    List<Remplacement> findByEnseignantRemplacantIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            UUID enseignantId, LocalDate fin, LocalDate debut);
    List<Remplacement> findByEmploiDuTempsIdIn(List<UUID> emploiDuTempsIds);
}
