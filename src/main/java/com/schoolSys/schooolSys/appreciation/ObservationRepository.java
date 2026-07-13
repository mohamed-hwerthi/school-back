package com.schoolSys.schooolSys.appreciation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ObservationRepository extends JpaRepository<ObservationTrimestre, UUID> {

    List<ObservationTrimestre> findByStudentIdInAndTrimestre(List<UUID> studentIds, Integer trimestre);

    Optional<ObservationTrimestre> findByStudentIdAndTrimestre(UUID studentId, Integer trimestre);
}
