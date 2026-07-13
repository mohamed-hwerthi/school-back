package com.schoolSys.schooolSys.rh.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateFichePaieRequest {
    @NotNull
    private UUID employeId;
    @NotNull
    private String employeType;
    @NotNull
    private Integer mois;
    @NotNull
    private Integer annee;
    @NotNull
    private BigDecimal salaireBase;
    private BigDecimal primes;
    private BigDecimal retenues;
    @NotNull
    private BigDecimal salaireNet;
    private LocalDate datePaiement;
    private Boolean paye;
    private String commentaire;
}
