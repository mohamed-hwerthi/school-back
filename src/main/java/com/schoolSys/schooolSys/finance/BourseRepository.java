package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BourseRepository extends JpaRepository<Bourse, UUID> {

    List<Bourse> findByAnneeScolaire(String anneeScolaire);

    List<Bourse> findByStudentId(UUID studentId);

    List<Bourse> findByStudentIdAndAnneeScolaire(UUID studentId, String anneeScolaire);
}
