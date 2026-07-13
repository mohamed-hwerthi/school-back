package com.schoolSys.schooolSys.facture.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureLigneDTO {
    private UUID id;
    @NotNull
    private String designation;
    private Integer quantite;
    @NotNull
    private BigDecimal prixUnitaire;
    private BigDecimal montant;
}
