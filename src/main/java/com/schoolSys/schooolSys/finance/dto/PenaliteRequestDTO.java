package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PenaliteRequestDTO {

    @NotNull(message = "L'élève est requis")
    private UUID studentId;

    private UUID paiementId;

    @NotNull(message = "Le montant est requis")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotBlank(message = "Le motif est requis")
    private String motif;

    private LocalDate dateApplication;

    private String anneeScolaire;

    private Boolean payee;
}
