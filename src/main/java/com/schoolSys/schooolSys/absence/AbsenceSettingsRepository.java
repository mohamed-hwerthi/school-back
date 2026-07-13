package com.schoolSys.schooolSys.absence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbsenceSettingsRepository extends JpaRepository<AbsenceSettings, UUID> {
}
