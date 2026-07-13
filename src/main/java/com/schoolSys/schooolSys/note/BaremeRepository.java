package com.schoolSys.schooolSys.note;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BaremeRepository extends JpaRepository<Bareme, UUID> {
    Optional<Bareme> findByActifTrue();
}
