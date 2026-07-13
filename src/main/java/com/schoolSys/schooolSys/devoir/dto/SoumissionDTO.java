package com.schoolSys.schooolSys.devoir.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoumissionDTO {

    private UUID id;
    private UUID devoirId;
    private String devoirTitre;
    private UUID eleveId;
    private String contenu;
    private String fichierUrl;
    private LocalDateTime dateSoumission;
    private BigDecimal note;
    private String commentaireCorrection;
    private Boolean corrige;
    private Boolean enRetard;
    private String anneeScolaire;
    private LocalDateTime createdAt;
}
