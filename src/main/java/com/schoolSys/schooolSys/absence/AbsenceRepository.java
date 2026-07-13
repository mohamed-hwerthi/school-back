package com.schoolSys.schooolSys.absence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, UUID> {

    List<Absence> findByEleveId(UUID eleveId);

    List<Absence> findByEleveIdIn(List<UUID> eleveIds);

    List<Absence> findByDateAndEleveIdIn(LocalDate date, List<UUID> eleveIds);

    List<Absence> findByDateAndEleveIdInAndType(LocalDate date, List<UUID> eleveIds, String type);

    @Query("SELECT a FROM Absence a WHERE a.eleveId IN :eleveIds AND MONTH(a.date) = :mois AND YEAR(a.date) = :annee")
    List<Absence> findByEleveIdInAndMonthAndYear(@Param("eleveIds") List<UUID> eleveIds,
                                                  @Param("mois") int mois,
                                                  @Param("annee") int annee);

    /** School-year-aware absence queries. */
    List<Absence> findByEleveIdAndAnneeScolaire(UUID eleveId, String anneeScolaire);

    List<Absence> findByEleveIdInAndAnneeScolaire(List<UUID> eleveIds, String anneeScolaire);

    List<Absence> findByDateAndEleveIdInAndAnneeScolaire(LocalDate date, List<UUID> eleveIds, String anneeScolaire);

    List<Absence> findByDateAndEleveIdInAndTypeAndAnneeScolaire(LocalDate date, List<UUID> eleveIds, String type, String anneeScolaire);

    @Query("SELECT a FROM Absence a WHERE a.eleveId IN :eleveIds AND MONTH(a.date) = :mois AND YEAR(a.date) = :annee AND a.anneeScolaire = :anneeScolaire")
    List<Absence> findByEleveIdInAndMonthAndYearAndAnneeScolaire(@Param("eleveIds") List<UUID> eleveIds,
                                                                  @Param("mois") int mois,
                                                                  @Param("annee") int annee,
                                                                  @Param("anneeScolaire") String anneeScolaire);

    List<Absence> findByEleveIdOrderByDateDesc(UUID eleveId);

    @Query("SELECT COUNT(a) FROM Absence a WHERE a.eleveId = :eleveId AND a.type = 'ABSENCE'")
    long countAbsencesByEleveId(@Param("eleveId") UUID eleveId);
}
