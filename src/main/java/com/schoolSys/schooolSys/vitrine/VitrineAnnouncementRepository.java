package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VitrineAnnouncementRepository extends JpaRepository<VitrineAnnouncement, UUID> {

    List<VitrineAnnouncement> findByPublishedTrueAndExpiresAtIsNullOrExpiresAtAfterOrderByPinnedDescCreatedAtDesc(LocalDateTime now);

    List<VitrineAnnouncement> findAllByOrderByPinnedDescCreatedAtDesc();
}
