package com.schoolSys.schooolSys.emploidutemps.dto;

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
public class EmploiDuTempsResponseDTO {
    private UUID id;
    private UUID classeId;
    private UUID creneauId;
    private Integer jourSemaine;
    private UUID moduleId;
    private UUID enseignantId;
    private String salle;
    private UUID classroomId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
