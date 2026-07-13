package com.schoolSys.schooolSys.anneescolaire;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacanceRepository extends JpaRepository<Vacance, UUID> {

    List<Vacance> findByAnneeScolaireId(UUID anneeScolaireId);
}
