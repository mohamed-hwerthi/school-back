package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, UUID> {

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    List<Vehicule> findByStatut(String statut);

    boolean existsByImmatriculation(String immatriculation);
}
