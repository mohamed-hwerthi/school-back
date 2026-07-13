package com.schoolSys.schooolSys.disponibilite;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnseignantDisponibiliteRepository
        extends JpaRepository<EnseignantDisponibilite, UUID> {

    List<EnseignantDisponibilite> findByEnseignantId(UUID enseignantId);

    List<EnseignantDisponibilite> findByEnseignantIdAndType(UUID enseignantId, String type);

    Optional<EnseignantDisponibilite> findByEnseignantIdAndJourSemaineAndCreneauId(
            UUID enseignantId, Integer jourSemaine, UUID creneauId);

    void deleteByEnseignantIdAndJourSemaineAndCreneauId(
            UUID enseignantId, Integer jourSemaine, UUID creneauId);
}
