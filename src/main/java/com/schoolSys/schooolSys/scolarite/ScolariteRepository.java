package com.schoolSys.schooolSys.scolarite;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScolariteRepository extends JpaRepository<Scolarite, UUID> {

    List<Scolarite> findByStudentIdOrderByAnneeScolaireDesc(UUID studentId);

    List<Scolarite> findByAnneeScolaire(String anneeScolaire);

    Optional<Scolarite> findByStudentIdAndAnneeScolaire(UUID studentId, String anneeScolaire);

    boolean existsByStudentIdAndAnneeScolaire(UUID studentId, String anneeScolaire);
}
