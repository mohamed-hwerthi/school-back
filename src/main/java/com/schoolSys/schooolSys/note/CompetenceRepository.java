package com.schoolSys.schooolSys.note;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompetenceRepository extends JpaRepository<Competence, UUID> {
    List<Competence> findByModuleId(UUID moduleId);
}
