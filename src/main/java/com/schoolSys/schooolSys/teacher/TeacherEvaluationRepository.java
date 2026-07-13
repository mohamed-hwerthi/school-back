package com.schoolSys.schooolSys.teacher;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherEvaluationRepository extends JpaRepository<TeacherEvaluation, UUID> {

    List<TeacherEvaluation> findByTeacherId(UUID teacherId);

    List<TeacherEvaluation> findByTeacherIdAndAnneeScolaire(UUID teacherId, String anneeScolaire);

    List<TeacherEvaluation> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    List<TeacherEvaluation> findByAnneeScolaire(String anneeScolaire);
}
