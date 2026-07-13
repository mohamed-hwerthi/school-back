package com.schoolSys.schooolSys.meeting;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByEnseignantIdOrderByDateAscHeureDebutAsc(UUID enseignantId);

    List<Meeting> findByParentIdOrderByDateAscHeureDebutAsc(UUID parentId);

    List<Meeting> findByStudentIdOrderByDateAscHeureDebutAsc(UUID studentId);

    List<Meeting> findByDateBetweenOrderByDateAscHeureDebutAsc(LocalDate start, LocalDate end);

    List<Meeting> findByStatutOrderByDateAscHeureDebutAsc(String statut);

    List<Meeting> findAllByOrderByDateAscHeureDebutAsc();
}
