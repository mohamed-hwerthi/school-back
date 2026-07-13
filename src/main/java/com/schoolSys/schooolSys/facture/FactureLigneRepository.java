package com.schoolSys.schooolSys.facture;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureLigneRepository extends JpaRepository<FactureLigne, UUID> {

    List<FactureLigne> findByFactureId(UUID factureId);
}
