package com.schoolSys.schooolSys.websocket;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks online users connected via WebSocket.
 * Listens for session connect/disconnect events to maintain the online user set.
 */
@Slf4j
@Component
public class OnlineUserTracker {

    /** Map of sessionId -> username (email) */
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    /** Map of sessionId -> userId */
    private final Map<String, UUID> sessionUserIdMap = new ConcurrentHashMap<>();

    /** Map of userId -> email for reverse lookup */
    private final Map<UUID, String> userIdEmailMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        if (principal != null && sessionId != null) {
            String username = principal.getName();
            sessionUserMap.put(sessionId, username);

            // Extract userId from session attributes if available
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
                UUID userId = (UUID) sessionAttributes.get("userId");
                sessionUserIdMap.put(sessionId, userId);
                userIdEmailMap.put(userId, username);
            }

            log.info("User connected: {} (session: {})", username, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            String username = sessionUserMap.remove(sessionId);
            UUID userId = sessionUserIdMap.remove(sessionId);

            // Only remove from userIdEmailMap if no other sessions exist for this user
            if (userId != null && !sessionUserIdMap.containsValue(userId)) {
                userIdEmailMap.remove(userId);
            }

            log.info("User disconnected: {} (session: {})", username, sessionId);
        }
    }

    /**
     * Returns the set of currently online usernames (emails).
     */
    public Set<String> getOnlineUsers() {
        return Set.copyOf(sessionUserMap.values());
    }

    /**
     * Checks if a user is currently online.
     *
     * @param username the user's email
     * @return true if the user has at least one active WebSocket session
     */
    public boolean isOnline(String username) {
        return sessionUserMap.containsValue(username);
    }

    /**
     * Gets the email address of a user by their ID.
     *
     * @param userId the user's ID
     * @return the user's email, or null if not currently online
     */
    public String getUserEmailById(UUID userId) {
        return userIdEmailMap.get(userId);
    }

    /**
     * Returns the count of currently online users.
     */
    public int getOnlineCount() {
        return (int) sessionUserMap.values().stream().distinct().count();
    }
}
