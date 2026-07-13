package com.schoolSys.schooolSys.settings;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, UUID> {
}
