package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PointagePersonnelRepository extends JpaRepository<PointagePersonnel, UUID> {

    List<PointagePersonnel> findByDatePointage(LocalDate date);

    List<PointagePersonnel> findByEmployeIdAndEmployeType(UUID employeId, String employeType);

    List<PointagePersonnel> findByEmployeIdAndDatePointageBetween(UUID employeId, LocalDate start, LocalDate end);

    List<PointagePersonnel> findByDatePointageBetween(LocalDate start, LocalDate end);

    long countByDatePointageAndStatut(LocalDate date, String statut);
}
