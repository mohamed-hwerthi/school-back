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
public class VitrineAnnouncementDTO {

    private UUID id;
    private String title;
    private String body;
    private boolean pinned;
    private boolean published;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
