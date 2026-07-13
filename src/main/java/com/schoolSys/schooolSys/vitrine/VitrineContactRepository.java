package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VitrineContactRepository extends JpaRepository<VitrineContact, UUID> {

    List<VitrineContact> findAllByOrderByCreatedAtDesc();

    long countByIsReadFalse();
}
