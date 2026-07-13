package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CongeRepository extends JpaRepository<Conge, UUID> {

    List<Conge> findByEnseignantId(UUID enseignantId);

    List<Conge> findByStatut(String statut);

    List<Conge> findByEnseignantIdAndStatut(UUID enseignantId, String statut);
}
