package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratEnseignantRepository extends JpaRepository<ContratEnseignant, UUID> {

    List<ContratEnseignant> findByEnseignantId(UUID enseignantId);

    List<ContratEnseignant> findByStatut(String statut);

    List<ContratEnseignant> findByEnseignantIdAndStatut(UUID enseignantId, String statut);
}
