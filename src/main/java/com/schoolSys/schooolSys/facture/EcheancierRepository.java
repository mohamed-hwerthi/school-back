package com.schoolSys.schooolSys.facture;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcheancierRepository extends JpaRepository<Echeancier, UUID> {

    List<Echeancier> findByStudentId(UUID studentId);

    List<Echeancier> findByAnneeScolaire(String anneeScolaire);

    List<Echeancier> findByStudentIdAndAnneeScolaire(UUID studentId, String anneeScolaire);
}
