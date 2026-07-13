package com.schoolSys.schooolSys.facture.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EcheancierRequestDTO {
    @NotNull
    private UUID studentId;
    @NotNull
    private String anneeScolaire;
    @NotNull
    private BigDecimal montantTotal;
    @NotNull
    private Integer nombreEcheances;
    private String statut;
    private String notes;
}
