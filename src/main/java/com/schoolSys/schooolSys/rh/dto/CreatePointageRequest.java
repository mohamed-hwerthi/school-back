package com.schoolSys.schooolSys.rh.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreatePointageRequest {
    @NotNull
    private UUID employeId;
    @NotNull
    private String employeType;
    private LocalDate datePointage;
    private LocalTime heureArrivee;
    private LocalTime heureDepart;
    private BigDecimal heuresTravaillees;
    private String statut;
    private String notes;
}
