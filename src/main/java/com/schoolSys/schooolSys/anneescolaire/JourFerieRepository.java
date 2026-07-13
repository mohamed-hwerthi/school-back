package com.schoolSys.schooolSys.anneescolaire;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JourFerieRepository extends JpaRepository<JourFerie, UUID> {

    List<JourFerie> findByAnneeScolaireId(UUID anneeScolaireId);
}
