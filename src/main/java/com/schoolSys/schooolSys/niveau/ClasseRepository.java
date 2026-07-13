package com.schoolSys.schooolSys.niveau;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClasseRepository extends JpaRepository<Classe, UUID> {
    List<Classe> findByNiveauId(UUID niveauId);
}
