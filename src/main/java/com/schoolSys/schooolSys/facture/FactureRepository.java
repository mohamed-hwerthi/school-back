package com.schoolSys.schooolSys.facture;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, UUID> {

    Optional<Facture> findByNumero(String numero);

    List<Facture> findByStudentId(UUID studentId);

    List<Facture> findByStatut(String statut);

    List<Facture> findByStudentIdAndStatut(UUID studentId, String statut);
}
