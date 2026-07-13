package com.schoolSys.schooolSys.common.audit;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks user actions for security auditing purposes.
 * Records who did what, on which entity, and when.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    /** Username of the actor (from SecurityContext). */
    @Column(name = "username", length = 200)
    private String username;

    /** Action performed: CREATE, UPDATE, DELETE, LOGIN, LOGOUT. */
    @Column(name = "action", length = 50, nullable = false)
    private String action;

    /** Type of entity affected: STUDENT, TEACHER, etc. */
    @Column(name = "entity_type", length = 100)
    private String entityType;

    /** Primary key of the affected entity. */
    @Column(name = "entity_id")
    private UUID entityId;

    /** JSON description of the changes or additional context. */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /** IP address of the client that triggered the action. */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /** When the action occurred. */
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
