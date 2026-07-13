package com.schoolSys.schooolSys.cantine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbonnementCantineRepository extends JpaRepository<AbonnementCantine, UUID> {

    List<AbonnementCantine> findByActifTrue();

    List<AbonnementCantine> findByEleveId(UUID eleveId);

    List<AbonnementCantine> findByEleveIdAndActifTrue(UUID eleveId);

    @Query("SELECT COUNT(a) FROM AbonnementCantine a WHERE a.actif = true")
    long countActifs();

    @Query("SELECT COALESCE(SUM(a.montant), 0) FROM AbonnementCantine a WHERE a.actif = true")
    java.math.BigDecimal sumMontantActifs();

    @Query("SELECT a.typeAbonnement, COUNT(a) FROM AbonnementCantine a WHERE a.actif = true GROUP BY a.typeAbonnement")
    List<Object[]> countActifsByType();
}
