package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationTransportRepository extends JpaRepository<AffectationTransport, UUID> {

    List<AffectationTransport> findByCircuitIdAndActifTrue(UUID circuitId);

    List<AffectationTransport> findByEleveIdAndActifTrue(UUID eleveId);

    List<AffectationTransport> findByCircuitId(UUID circuitId);

    List<AffectationTransport> findByEleveId(UUID eleveId);

    @Query("SELECT COUNT(a) FROM AffectationTransport a WHERE a.actif = true")
    long countActifs();

    @Query("SELECT COUNT(a) FROM AffectationTransport a WHERE a.circuit.id = :circuitId AND a.actif = true")
    long countByCircuitIdAndActifTrue(@Param("circuitId") UUID circuitId);
}
