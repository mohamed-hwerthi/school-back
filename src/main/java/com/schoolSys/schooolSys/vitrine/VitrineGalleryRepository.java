package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VitrineGalleryRepository extends JpaRepository<VitrineGallery, UUID> {

    List<VitrineGallery> findAllByOrderByDisplayOrderAsc();
}
