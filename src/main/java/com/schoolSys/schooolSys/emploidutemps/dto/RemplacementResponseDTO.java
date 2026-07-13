package com.schoolSys.schooolSys.emploidutemps.dto;

import java.util.UUID;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RemplacementResponseDTO {
    private UUID id;
    private UUID emploiDuTempsId;
    private UUID enseignantRemplacantId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
    private LocalDateTime createdAt;
}
