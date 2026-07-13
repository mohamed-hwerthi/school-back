package com.schoolSys.schooolSys.discipline.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SanctionRequestDTO {
    @NotNull
    private UUID eleveId;
    private UUID incidentId;
    @NotBlank
    private String type;
    private String description;
    @NotNull
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private UUID decideParId;
    private Boolean notifieParents;
    private Integer niveau;
    private String statut;
}
