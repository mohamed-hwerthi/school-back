package com.schoolSys.schooolSys.inscription;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InscriptionRepository extends JpaRepository<Inscription, UUID> {

    Page<Inscription> findByStatut(String statut, Pageable pageable);

    Page<Inscription> findByAnneeScolaire(String anneeScolaire, Pageable pageable);

    Page<Inscription> findByStatutAndAnneeScolaire(String statut, String anneeScolaire, Pageable pageable);

    Page<Inscription> findByNiveauId(UUID niveauId, Pageable pageable);

    Page<Inscription> findByStatutAndNiveauId(String statut, UUID niveauId, Pageable pageable);

    Page<Inscription> findByAnneeScolaireAndNiveauId(String anneeScolaire, UUID niveauId, Pageable pageable);

    Page<Inscription> findByStatutAndAnneeScolaireAndNiveauId(String statut, String anneeScolaire, UUID niveauId, Pageable pageable);

    Optional<Inscription> findByNumeroDossier(String numeroDossier);

    long countByNiveauIdAndAnneeScolaireAndStatut(UUID niveauId, String anneeScolaire, String statut);

    long countByStatut(String statut);

    long countByAnneeScolaire(String anneeScolaire);

    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.anneeScolaire = :annee AND i.statut = :statut")
    long countByAnneeScolaireAndStatut(@Param("annee") String anneeScolaire, @Param("statut") String statut);
}
