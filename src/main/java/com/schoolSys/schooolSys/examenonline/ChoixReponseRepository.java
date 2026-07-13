package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoixReponseRepository extends JpaRepository<ChoixReponse, UUID> {

    List<ChoixReponse> findByQuestionIdOrderByOrdreAsc(UUID questionId);
}
