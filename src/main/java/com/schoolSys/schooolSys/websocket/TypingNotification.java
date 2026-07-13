package com.schoolSys.schooolSys.websocket;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for typing indicator notifications sent via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingNotification {

    private UUID senderId;

    private String senderName;

    private UUID recipientId;

    private boolean typing;
}
