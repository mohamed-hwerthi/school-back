package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReponseEleveRepository extends JpaRepository<ReponseEleve, UUID> {

    List<ReponseEleve> findByTentativeIdOrderByQuestionOrdreAsc(UUID tentativeId);

    List<ReponseEleve> findByTentativeIdAndQuestionId(UUID tentativeId, UUID questionId);
}
