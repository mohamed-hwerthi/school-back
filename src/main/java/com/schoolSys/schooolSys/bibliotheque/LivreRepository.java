package com.schoolSys.schooolSys.bibliotheque;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LivreRepository extends JpaRepository<Livre, UUID> {

    Page<Livre> findByCategorie(String categorie, Pageable pageable);

    Page<Livre> findByTitreContainingIgnoreCase(String titre, Pageable pageable);

    Optional<Livre> findByIsbn(String isbn);

    Page<Livre> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase(
            String titre, String auteur, Pageable pageable);

    Page<Livre> findByCategorieAndTitreContainingIgnoreCaseOrCategorieAndAuteurContainingIgnoreCase(
            String categorie1, String titre, String categorie2, String auteur, Pageable pageable);

    List<Livre> findByCategorie(String categorie);
}
