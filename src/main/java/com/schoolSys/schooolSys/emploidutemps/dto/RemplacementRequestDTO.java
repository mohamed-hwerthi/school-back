package com.schoolSys.schooolSys.emploidutemps.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RemplacementRequestDTO {
    @NotNull private UUID emploiDuTempsId;
    @NotNull private UUID enseignantRemplacantId;
    @NotNull private LocalDate dateDebut;
    @NotNull private LocalDate dateFin;
    private String motif;
}
