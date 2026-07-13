package com.schoolSys.schooolSys.niveau;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NiveauRepository extends JpaRepository<Niveau, UUID> {

    boolean existsByName(String name);

    Optional<Niveau> findByName(String name);
}
