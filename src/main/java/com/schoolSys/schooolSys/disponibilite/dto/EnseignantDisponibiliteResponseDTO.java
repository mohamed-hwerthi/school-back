package com.schoolSys.schooolSys.disponibilite.dto;

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
public class EnseignantDisponibiliteResponseDTO {

    private UUID id;
    private UUID enseignantId;
    private Integer jourSemaine;
    private UUID creneauId;
    private String type;
    private String motif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
