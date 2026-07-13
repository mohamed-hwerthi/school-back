package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VitrineConfigRepository extends JpaRepository<VitrineConfig, UUID> {

    Optional<VitrineConfig> findFirstByOrderByIdAsc();
}
