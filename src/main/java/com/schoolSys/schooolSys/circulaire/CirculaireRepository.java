package com.schoolSys.schooolSys.circulaire;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CirculaireRepository extends JpaRepository<Circulaire, UUID> {

    List<Circulaire> findByTypeOrderByDateCreationDesc(String type);

    List<Circulaire> findByStatutOrderByDateCreationDesc(String statut);

    List<Circulaire> findByCibleOrderByDateCreationDesc(String cible);

    List<Circulaire> findByTitreContainingIgnoreCaseOrderByDateCreationDesc(String search);

    List<Circulaire> findAllByOrderByDateCreationDesc();
}
