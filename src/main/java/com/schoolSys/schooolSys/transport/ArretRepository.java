package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArretRepository extends JpaRepository<Arret, UUID> {

    List<Arret> findByCircuitIdOrderByOrdreAsc(UUID circuitId);
}
