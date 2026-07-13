package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormationRepository extends JpaRepository<Formation, UUID> {

    List<Formation> findByStatut(String statut);
}
