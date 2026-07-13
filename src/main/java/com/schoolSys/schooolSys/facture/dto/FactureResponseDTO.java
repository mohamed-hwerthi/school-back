package com.schoolSys.schooolSys.facture.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureResponseDTO {
    private UUID id;
    private String numero;
    private UUID studentId;
    private String studentNom;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private BigDecimal montantTotal;
    private BigDecimal montantPaye;
    private String statut;
    private String notes;
    private List<FactureLigneDTO> lignes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
