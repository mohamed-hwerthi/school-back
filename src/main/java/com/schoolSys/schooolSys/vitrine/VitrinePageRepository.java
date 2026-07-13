package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VitrinePageRepository extends JpaRepository<VitrinePage, UUID> {

    List<VitrinePage> findAllByOrderByDisplayOrderAsc();

    List<VitrinePage> findByVisibleTrueOrderByDisplayOrderAsc();

    Optional<VitrinePage> findBySlug(String slug);
}
