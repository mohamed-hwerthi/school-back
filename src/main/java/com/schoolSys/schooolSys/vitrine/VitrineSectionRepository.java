package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VitrineSectionRepository extends JpaRepository<VitrineSection, UUID> {

    List<VitrineSection> findByPageIdOrderByDisplayOrderAsc(UUID pageId);
}
