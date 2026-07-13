package com.schoolSys.schooolSys.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket controller handling real-time chat messaging.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final OnlineUserTracker onlineUserTracker;

    /**
     * Receives a chat message and forwards it to the recipient's personal queue.
     * Also sends the message back to the sender for confirmation.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setSenderName(principal != null ? principal.getName() : chatMessage.getSenderName());

        log.debug("Chat message from {} to recipient {}", chatMessage.getSenderName(), chatMessage.getRecipientId());

        // Send to recipient
        String recipientEmail = onlineUserTracker.getUserEmailById(chatMessage.getRecipientId());
        if (recipientEmail != null) {
            messagingTemplate.convertAndSendToUser(
                    recipientEmail,
                    "/queue/chat",
                    chatMessage
            );
        }

        // Echo back to sender for confirmation
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/chat",
                    chatMessage
            );
        }
    }

    /**
     * Receives a typing notification and forwards it to the recipient.
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingNotification notification, Principal principal) {
        notification.setSenderName(principal != null ? principal.getName() : notification.getSenderName());

        String recipientEmail = onlineUserTracker.getUserEmailById(notification.getRecipientId());
        if (recipientEmail != null) {
            messagingTemplate.convertAndSendToUser(
                    recipientEmail,
                    "/queue/chat.typing",
                    notification
            );
        }
    }
}
