package com.schoolSys.schooolSys.emploidutemps.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmploiDuTempsRequestDTO {
    @NotNull
    private UUID creneauId;
    @NotNull
    private Integer jourSemaine;
    private UUID moduleId;
    private UUID enseignantId;
    private String salle;
}
