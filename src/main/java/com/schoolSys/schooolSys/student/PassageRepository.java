package com.schoolSys.schooolSys.student;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassageRepository extends JpaRepository<Passage, UUID> {

    List<Passage> findByAnneeScolaire(String anneeScolaire);

    List<Passage> findByStudentId(UUID studentId);

    List<Passage> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<Passage> findByAnneeScolaireAndDecision(String anneeScolaire, String decision);
}
