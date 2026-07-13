package com.schoolSys.schooolSys.rapport;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RapportRepository extends JpaRepository<Rapport, UUID> {

    List<Rapport> findByTypeOrderByDateGenerationDesc(String type);

    List<Rapport> findByStatutOrderByDateGenerationDesc(String statut);

    List<Rapport> findAllByOrderByDateGenerationDesc();
}
