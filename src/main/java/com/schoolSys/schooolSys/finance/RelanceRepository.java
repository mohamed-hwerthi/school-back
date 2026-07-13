package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RelanceRepository extends JpaRepository<Relance, UUID> {

    List<Relance> findByAnneeScolaireOrderByCreatedAtDesc(String anneeScolaire);

    List<Relance> findByStudentIdAndAnneeScolaireOrderByCreatedAtDesc(UUID studentId, String anneeScolaire);

    List<Relance> findByStatutAndAnneeScolaireOrderByDatePrevueAsc(
            Relance.StatutRelance statut, String anneeScolaire);

    long countByAnneeScolaireAndStatut(String anneeScolaire, Relance.StatutRelance statut);

    long countByAnneeScolaire(String anneeScolaire);

    @Query("SELECT MAX(r.numeroRelance) FROM Relance r WHERE r.student.id = :studentId AND r.anneeScolaire = :annee")
    Integer findMaxNumeroRelanceByStudentAndAnnee(@Param("studentId") UUID studentId, @Param("annee") String annee);

    boolean existsByStudentIdAndPaiementIdAndStatut(UUID studentId, UUID paiementId, Relance.StatutRelance statut);
}
