package com.schoolSys.schooolSys.facture.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcheancierResponseDTO {
    private UUID id;
    private UUID studentId;
    private String studentNom;
    private String anneeScolaire;
    private BigDecimal montantTotal;
    private Integer nombreEcheances;
    private String statut;
    private String notes;
    private List<EcheanceDTO> echeances;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
