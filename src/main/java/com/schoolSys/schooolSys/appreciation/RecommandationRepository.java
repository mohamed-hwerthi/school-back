package com.schoolSys.schooolSys.appreciation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommandationRepository extends JpaRepository<Recommandation, UUID> {

    List<Recommandation> findByStudentIdInAndTrimestre(List<UUID> studentIds, Integer trimestre);

    Optional<Recommandation> findByStudentIdAndDomaineIdAndTrimestre(
            UUID studentId, UUID domaineId, Integer trimestre);
}
