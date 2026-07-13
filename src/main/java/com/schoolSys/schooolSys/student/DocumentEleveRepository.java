package com.schoolSys.schooolSys.student;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentEleveRepository extends JpaRepository<DocumentEleve, UUID> {
    List<DocumentEleve> findByStudentIdOrderByUploadedAtDesc(UUID studentId);
}
