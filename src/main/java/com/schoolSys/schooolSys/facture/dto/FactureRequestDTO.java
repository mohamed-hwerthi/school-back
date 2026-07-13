package com.schoolSys.schooolSys.facture.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FactureRequestDTO {
    @NotNull
    private UUID studentId;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private String statut;
    private String notes;
    private List<FactureLigneDTO> lignes;
}
