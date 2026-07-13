package com.schoolSys.schooolSys.bibliotheque.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.bibliotheque.Emprunt;
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
public class EmpruntDTO {

    private UUID id;
    private UUID livreId;
    private String livreTitle;
    private UUID eleveId;
    private String eleveName;
    private LocalDate dateEmprunt;
    private LocalDate dateRetourPrevue;
    private LocalDate dateRetourEffective;
    private Emprunt.StatutEmprunt statut;
    private BigDecimal penalite;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
