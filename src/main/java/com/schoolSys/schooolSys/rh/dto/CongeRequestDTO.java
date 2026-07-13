package com.schoolSys.schooolSys.rh.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CongeRequestDTO {
    @NotNull
    private UUID enseignantId;
    @NotNull
    private String typeConge;
    @NotNull
    private LocalDate dateDebut;
    @NotNull
    private LocalDate dateFin;
    private String motif;
    private String statut;
}
