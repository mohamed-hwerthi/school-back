package com.schoolSys.schooolSys.vitrine.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitrineContactDTO {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private boolean read;
    private boolean replied;
    private String replyText;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}
