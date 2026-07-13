package com.schoolSys.schooolSys.module;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, UUID> {

    List<Module> findByNiveauIdOrderByOrdreEtatiqueAsc(UUID niveauId);

    List<Module> findAllByOrderByNiveauNameAscOrdreEtatiqueAsc();
}
