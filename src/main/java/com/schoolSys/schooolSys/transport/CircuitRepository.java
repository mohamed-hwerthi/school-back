package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CircuitRepository extends JpaRepository<Circuit, UUID> {

    List<Circuit> findByActifTrue();

    @Query("SELECT c FROM Circuit c LEFT JOIN FETCH c.vehicule LEFT JOIN FETCH c.arrets WHERE c.id = :id")
    Circuit findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT c FROM Circuit c LEFT JOIN FETCH c.vehicule")
    List<Circuit> findAllWithVehicule();
}
