package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FichePaieRepository extends JpaRepository<FichePaie, UUID> {

    List<FichePaie> findByEmployeId(UUID employeId);

    List<FichePaie> findByMoisAndAnnee(Integer mois, Integer annee);

    List<FichePaie> findByAnnee(Integer annee);

    List<FichePaie> findByPaye(Boolean paye);

    @Query("SELECT COALESCE(SUM(f.salaireNet), 0) FROM FichePaie f WHERE f.mois = :mois AND f.annee = :annee")
    BigDecimal sumSalaireNetByMoisAndAnnee(@Param("mois") Integer mois, @Param("annee") Integer annee);
}
