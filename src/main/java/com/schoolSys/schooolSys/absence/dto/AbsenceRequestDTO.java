package com.schoolSys.schooolSys.absence.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AbsenceRequestDTO {
    @NotNull
    private UUID eleveId;
    @NotNull
    private LocalDate date;
    @NotNull
    private String type; // ABSENCE or RETARD
    private String seance;
    private LocalTime heureArrivee;
    private Boolean justifie;
    private String motif;
    private UUID enseignantId;
}
