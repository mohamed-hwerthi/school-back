package com.schoolSys.schooolSys.examen;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamenRepository extends JpaRepository<Examen, UUID> {

    @Query("SELECT e FROM Examen e " +
            "WHERE (:moduleId IS NULL OR e.module.id = :moduleId) " +
            "AND (:classeId IS NULL OR e.classe.id = :classeId) " +
            "AND (:trimestre IS NULL OR e.trimestre = :trimestre) " +
            "AND (:anneeScolaire IS NULL OR e.anneeScolaire = :anneeScolaire) " +
            "ORDER BY e.module.niveau.name ASC, e.module.name ASC, " +
            "e.trimestre ASC, e.ordreEtatique ASC")
    List<Examen> findFiltered(
            @Param("moduleId") UUID moduleId,
            @Param("classeId") UUID classeId,
            @Param("trimestre") Integer trimestre,
            @Param("anneeScolaire") String anneeScolaire);

    /** Whether an exam already exists for this (matière, classe, trimestre, année) quadruple. */
    boolean existsByModuleIdAndClasseIdAndTrimestreAndAnneeScolaire(UUID moduleId, UUID classeId, Integer trimestre, String anneeScolaire);
}
