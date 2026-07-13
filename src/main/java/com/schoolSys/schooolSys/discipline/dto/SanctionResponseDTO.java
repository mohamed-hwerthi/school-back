package com.schoolSys.schooolSys.discipline.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanctionResponseDTO {
    private UUID id;
    private UUID eleveId;
    private UUID incidentId;
    private String type;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private UUID decideParId;
    private Boolean notifieParents;
    private Integer niveau;
    private String statut;
    private UUID approuvePar;
    private String commentaireApprobation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
