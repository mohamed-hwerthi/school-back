package com.schoolSys.schooolSys.anneescolaire;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrimestreRepository extends JpaRepository<Trimestre, UUID> {

    List<Trimestre> findByAnneeScolaireId(UUID anneeScolaireId);
}
