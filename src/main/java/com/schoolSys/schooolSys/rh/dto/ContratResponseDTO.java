package com.schoolSys.schooolSys.rh.dto;

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
public class ContratResponseDTO {
    private UUID id;
    private UUID enseignantId;
    private String enseignantNom;
    private String typeContrat;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal salaire;
    private String statut;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
