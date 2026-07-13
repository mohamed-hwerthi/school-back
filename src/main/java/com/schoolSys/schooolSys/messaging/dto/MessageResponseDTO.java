package com.schoolSys.schooolSys.messaging.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private UUID id;
    private UUID senderId;
    private String subject;
    private String body;
    private String type;
    private List<RecipientDTO> recipients;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientDTO {
        private UUID id;
        private UUID recipientId;
        private LocalDateTime readAt;
        private Boolean deleted;
    }
}
