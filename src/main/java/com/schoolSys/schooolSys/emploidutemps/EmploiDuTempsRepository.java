package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, UUID> {

    List<EmploiDuTemps> findByClasseId(UUID classeId);

    List<EmploiDuTemps> findByEnseignantId(UUID enseignantId);

    List<EmploiDuTemps> findByEnseignantIdAndJourSemaineAndCreneauId(UUID enseignantId, Integer jourSemaine, UUID creneauId);

    List<EmploiDuTemps> findBySalleAndJourSemaineAndCreneauId(String salle, Integer jourSemaine, UUID creneauId);

    void deleteByClasseId(UUID classeId);
}
