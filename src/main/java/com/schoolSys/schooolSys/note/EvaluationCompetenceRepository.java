package com.schoolSys.schooolSys.note;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluationCompetenceRepository extends JpaRepository<EvaluationCompetence, UUID> {
    List<EvaluationCompetence> findByEleveIdAndExamenId(UUID eleveId, UUID examenId);
    List<EvaluationCompetence> findByExamenId(UUID examenId);
}
