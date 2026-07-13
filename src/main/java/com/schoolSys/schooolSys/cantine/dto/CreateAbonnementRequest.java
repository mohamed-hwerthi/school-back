package com.schoolSys.schooolSys.cantine.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAbonnementRequest {
    @NotNull
    private UUID eleveId;
    @NotNull
    private String typeAbonnement;
    @NotNull
    private LocalDate dateDebut;
    private LocalDate dateFin;
    @NotNull
    private BigDecimal montant;
    private String allergies;
    private String regime;
}
