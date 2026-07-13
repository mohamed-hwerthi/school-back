package com.schoolSys.schooolSys.bibliotheque;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, UUID> {

    List<Emprunt> findByEleveId(UUID eleveId);

    List<Emprunt> findByLivreId(UUID livreId);

    Page<Emprunt> findByStatut(Emprunt.StatutEmprunt statut, Pageable pageable);

    List<Emprunt> findByStatut(Emprunt.StatutEmprunt statut);

    List<Emprunt> findByDateRetourPrevueBeforeAndStatut(LocalDate date, Emprunt.StatutEmprunt statut);

    long countByStatut(Emprunt.StatutEmprunt statut);

    @Query("SELECT e.livre.id, e.livre.titre, COUNT(e) as cnt " +
           "FROM Emprunt e GROUP BY e.livre.id, e.livre.titre ORDER BY cnt DESC")
    List<Object[]> findLivresLesPlusEmpruntes(Pageable pageable);
}
