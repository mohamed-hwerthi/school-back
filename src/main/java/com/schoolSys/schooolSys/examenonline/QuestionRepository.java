package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByQuizIdOrderByOrdreAsc(UUID quizId);

    long countByQuizId(UUID quizId);
}
