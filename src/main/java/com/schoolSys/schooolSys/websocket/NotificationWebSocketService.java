package com.schoolSys.schooolSys.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending real-time notifications via WebSocket.
 * Supports sending to individual users, all users, or users with a specific role.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a notification to a specific user via their personal queue.
     *
     * @param username the target user's email (used as principal name)
     * @param msg      the notification message
     */
    public void sendToUser(String username, NotificationMessage msg) {
        log.debug("Sending notification to user {}: {}", username, msg.getTitle());
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                msg
        );
    }

    /**
     * Broadcasts a notification to all connected users.
     *
     * @param msg the notification message
     */
    public void sendToAll(NotificationMessage msg) {
        log.debug("Broadcasting notification to all: {}", msg.getTitle());
        messagingTemplate.convertAndSend("/topic/notifications", msg);
    }

    /**
     * Sends a notification to all users subscribed to a specific role topic.
     *
     * @param role the target role (e.g., "ADMIN", "ENSEIGNANT", "PARENT")
     * @param msg  the notification message
     */
    public void sendToRole(String role, NotificationMessage msg) {
        log.debug("Sending notification to role {}: {}", role, msg.getTitle());
        messagingTemplate.convertAndSend("/topic/notifications/" + role, msg);
    }
}
