package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TentativeRepository extends JpaRepository<Tentative, UUID> {

    List<Tentative> findByQuizIdOrderByDateDebutDesc(UUID quizId);

    List<Tentative> findByEleveIdOrderByDateDebutDesc(UUID eleveId);

    List<Tentative> findByQuizIdAndEleveId(UUID quizId, UUID eleveId);

    long countByQuizId(UUID quizId);

    long countByQuizIdAndStatut(UUID quizId, String statut);
}
