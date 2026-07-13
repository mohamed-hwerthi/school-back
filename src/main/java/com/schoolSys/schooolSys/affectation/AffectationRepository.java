package com.schoolSys.schooolSys.affectation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, UUID> {

    List<Affectation> findByAnneeScolaireOrderByTeacherIdAsc(String anneeScolaire);

    List<Affectation> findByTeacherIdAndAnneeScolaire(UUID teacherId, String anneeScolaire);

    List<Affectation> findByClasseIdAndAnneeScolaire(UUID classeId, String anneeScolaire);

    @Query("SELECT a FROM Affectation a " +
           "WHERE (:teacherId IS NULL OR a.teacherId = :teacherId) " +
           "AND (:classeId IS NULL OR a.classeId = :classeId) " +
           "AND (:moduleId IS NULL OR a.moduleId = :moduleId) " +
           "AND (:anneeScolaire IS NULL OR a.anneeScolaire = :anneeScolaire) " +
           "ORDER BY a.teacherId ASC, a.classeId ASC")
    List<Affectation> search(@Param("teacherId") UUID teacherId,
                             @Param("classeId") UUID classeId,
                             @Param("moduleId") UUID moduleId,
                             @Param("anneeScolaire") String anneeScolaire);

    /** Used to enforce the unique-triplet rule (teacher, classe, module, annee). */
    @Query("SELECT COUNT(a) FROM Affectation a " +
           "WHERE a.teacherId = :teacherId " +
           "AND a.classeId = :classeId " +
           "AND ((:moduleId IS NULL AND a.moduleId IS NULL) OR a.moduleId = :moduleId) " +
           "AND a.anneeScolaire = :anneeScolaire " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId)")
    long countDuplicate(@Param("teacherId") UUID teacherId,
                        @Param("classeId") UUID classeId,
                        @Param("moduleId") UUID moduleId,
                        @Param("anneeScolaire") String anneeScolaire,
                        @Param("excludeId") UUID excludeId);

    /** Distinct classe_ids the teacher is assigned to (used later for scoping devoirs/notes). */
    @Query("SELECT DISTINCT a.classeId FROM Affectation a " +
           "WHERE a.teacherId = :teacherId " +
           "AND a.anneeScolaire = :anneeScolaire")
    List<UUID> findClasseIdsByTeacher(@Param("teacherId") UUID teacherId,
                                      @Param("anneeScolaire") String anneeScolaire);
}
