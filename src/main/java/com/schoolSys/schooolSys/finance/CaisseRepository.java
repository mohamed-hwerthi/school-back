package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaisseRepository extends JpaRepository<Caisse, UUID> {

    List<Caisse> findByAnneeScolaireOrderByCreatedAtDesc(String anneeScolaire);

    Optional<Caisse> findByStatutAndAnneeScolaire(Caisse.StatutCaisse statut, String anneeScolaire);

    boolean existsByStatutAndAnneeScolaire(Caisse.StatutCaisse statut, String anneeScolaire);
}
