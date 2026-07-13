package com.schoolSys.schooolSys.devoir;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RessourcePedagogiqueRepository extends JpaRepository<RessourcePedagogique, UUID> {

    List<RessourcePedagogique> findByModuleIdOrderByCreatedAtDesc(UUID moduleId);

    List<RessourcePedagogique> findByEnseignantIdOrderByCreatedAtDesc(UUID enseignantId);

    List<RessourcePedagogique> findByTypeOrderByCreatedAtDesc(String type);

    List<RessourcePedagogique> findAllByOrderByCreatedAtDesc();

    // Year-aware queries
    List<RessourcePedagogique> findByAnneeScolaireOrderByCreatedAtDesc(String anneeScolaire);

    List<RessourcePedagogique> findByModuleIdAndAnneeScolaireOrderByCreatedAtDesc(UUID moduleId, String anneeScolaire);
}
