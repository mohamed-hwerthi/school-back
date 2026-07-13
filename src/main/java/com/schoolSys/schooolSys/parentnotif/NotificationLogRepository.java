package com.schoolSys.schooolSys.parentnotif;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    @Query("SELECT n FROM NotificationLog n " +
            "WHERE (:recipientId IS NULL OR n.recipientId = :recipientId) " +
            "AND (:eventType IS NULL OR n.eventType = :eventType) " +
            "AND (:status IS NULL OR n.status = :status) " +
            "ORDER BY n.createdAt DESC")
    Page<NotificationLog> findFiltered(
            @Param("recipientId") UUID recipientId,
            @Param("eventType") ParentNotificationEvent eventType,
            @Param("status") NotificationLog.Status status,
            Pageable pageable);
}
