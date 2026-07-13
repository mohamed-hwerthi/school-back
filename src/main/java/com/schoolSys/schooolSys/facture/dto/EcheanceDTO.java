package com.schoolSys.schooolSys.facture.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcheanceDTO {
    private UUID id;
    private Integer numero;
    private LocalDate dateEcheance;
    private BigDecimal montant;
    private BigDecimal montantPaye;
    private String statut;
    private LocalDate datePaiement;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
