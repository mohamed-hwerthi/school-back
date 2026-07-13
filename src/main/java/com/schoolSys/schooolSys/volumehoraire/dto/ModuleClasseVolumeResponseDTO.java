package com.schoolSys.schooolSys.volumehoraire.dto;

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
public class ModuleClasseVolumeResponseDTO {

    private UUID id;
    private UUID moduleId;
    private UUID classeId;
    private UUID enseignantId;
    private UUID anneeScolaireId;
    private Integer nbHeuresHebdo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
