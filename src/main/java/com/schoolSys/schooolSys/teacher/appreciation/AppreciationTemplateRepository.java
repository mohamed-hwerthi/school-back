package com.schoolSys.schooolSys.teacher.appreciation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppreciationTemplateRepository extends JpaRepository<AppreciationTemplate, UUID> {

    List<AppreciationTemplate> findByEnseignantIdOrderByLibelleAsc(UUID enseignantId);
}
