package com.schoolSys.schooolSys.domaine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SousDomaineRepository extends JpaRepository<SousDomaine, UUID> {

    List<SousDomaine> findByDomaineIdOrderByOrdreAsc(UUID domaineId);
}
