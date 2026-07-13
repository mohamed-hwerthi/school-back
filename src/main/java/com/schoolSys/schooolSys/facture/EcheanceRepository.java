package com.schoolSys.schooolSys.facture;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcheanceRepository extends JpaRepository<Echeance, UUID> {

    List<Echeance> findByEcheancierId(UUID echeancierId);

    List<Echeance> findByStatut(String statut);
}
