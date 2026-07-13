package com.schoolSys.schooolSys.absence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JustificatifRepository extends JpaRepository<Justificatif, UUID> {

    List<Justificatif> findByAbsenceId(UUID absenceId);
}
