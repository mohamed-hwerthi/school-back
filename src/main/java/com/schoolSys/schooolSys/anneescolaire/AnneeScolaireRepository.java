package com.schoolSys.schooolSys.anneescolaire;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, UUID> {

    Optional<AnneeScolaire> findByActiveTrue();

    Optional<AnneeScolaire> findByLabel(String label);
}
