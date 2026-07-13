package com.schoolSys.schooolSys.domaine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DomaineRepository extends JpaRepository<Domaine, UUID> {

    List<Domaine> findByNiveauIdOrderByOrdreAsc(UUID niveauId);

    List<Domaine> findAllByOrderByNiveauNameAscOrdreAsc();
}
