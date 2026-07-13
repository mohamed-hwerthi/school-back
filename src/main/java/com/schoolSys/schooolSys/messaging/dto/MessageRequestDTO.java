package com.schoolSys.schooolSys.messaging.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MessageRequestDTO {
    @NotNull
    private UUID senderId;
    @NotBlank
    private String subject;
    @NotBlank
    private String body;
    private String type; // MESSAGE, CIRCULAIRE
    @NotEmpty
    private List<UUID> recipientIds;
}
