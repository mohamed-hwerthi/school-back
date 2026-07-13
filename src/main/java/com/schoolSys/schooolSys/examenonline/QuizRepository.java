package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    List<Quiz> findByClasseIdOrderByCreatedAtDesc(UUID classeId);

    List<Quiz> findByModuleIdOrderByCreatedAtDesc(UUID moduleId);

    List<Quiz> findByStatutOrderByCreatedAtDesc(String statut);

    List<Quiz> findByEnseignantIdOrderByCreatedAtDesc(UUID enseignantId);

    List<Quiz> findAllByOrderByCreatedAtDesc();
}
