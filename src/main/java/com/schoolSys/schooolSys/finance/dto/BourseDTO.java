package com.schoolSys.schooolSys.finance.dto;

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
public class BourseDTO {

    private UUID id;
    private UUID studentId;
    private String studentName;
    private String type;
    private String label;
    private BigDecimal montant;
    private BigDecimal pourcentage;
    private String anneeScolaire;
    private String statut;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
