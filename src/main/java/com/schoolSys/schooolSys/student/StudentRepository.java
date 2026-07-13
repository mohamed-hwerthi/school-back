package com.schoolSys.schooolSys.student;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    long countByClasse(String classe);
    List<Student> findByClasse(String classe);
    boolean existsByMatricule(String matricule);
    Optional<Student> findByMatricule(String matricule);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByEmail(String email);
}
