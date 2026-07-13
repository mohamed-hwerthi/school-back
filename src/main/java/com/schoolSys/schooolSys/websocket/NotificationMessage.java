package com.schoolSys.schooolSys.websocket;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for real-time notification messages sent via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    private UUID id;

    private NotificationType type;

    private String title;

    private String message;

    private String link;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private boolean read = false;

    public enum NotificationType {
        ABSENCE,
        NOTE,
        PAIEMENT,
        INSCRIPTION,
        SYSTEM,
        DISCIPLINE,
        EXAMEN,
        DEVOIR
    }
}
