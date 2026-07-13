package com.schoolSys.schooolSys.websocket;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for real-time chat messages sent via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private UUID senderId;

    private String senderName;

    private UUID recipientId;

    private String content;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    public enum MessageType {
        TEXT,
        SYSTEM
    }
}
