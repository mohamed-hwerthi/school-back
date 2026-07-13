package com.schoolSys.schooolSys.devoir;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevoirRepository extends JpaRepository<Devoir, UUID> {

    List<Devoir> findByClasseIdOrderByDateLimiteDesc(UUID classeId);

    List<Devoir> findByModuleIdOrderByDateLimiteDesc(UUID moduleId);

    List<Devoir> findByClasseIdAndModuleIdOrderByDateLimiteDesc(UUID classeId, UUID moduleId);

    List<Devoir> findByEnseignantIdOrderByDateLimiteDesc(UUID enseignantId);

    List<Devoir> findByStatutOrderByDateLimiteDesc(String statut);

    List<Devoir> findAllByOrderByDateLimiteDesc();

    // Year-aware queries
    List<Devoir> findByAnneeScolaireOrderByDateLimiteDesc(String anneeScolaire);

    List<Devoir> findByClasseIdAndAnneeScolaireOrderByDateLimiteDesc(UUID classeId, String anneeScolaire);

    List<Devoir> findByModuleIdAndAnneeScolaireOrderByDateLimiteDesc(UUID moduleId, String anneeScolaire);

    List<Devoir> findByClasseIdAndModuleIdAndAnneeScolaireOrderByDateLimiteDesc(UUID classeId, UUID moduleId, String anneeScolaire);

    List<Devoir> findByEnseignantIdAndAnneeScolaireOrderByDateLimiteDesc(UUID enseignantId, String anneeScolaire);

    List<Devoir> findByStatutAndAnneeScolaireOrderByDateLimiteDesc(String statut, String anneeScolaire);
}
