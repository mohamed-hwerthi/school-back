package com.schoolSys.schooolSys.devoir.dto;

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
public class DevoirDTO {

    private UUID id;
    private String titre;
    private String description;
    private UUID moduleId;
    private UUID classeId;
    private UUID enseignantId;
    private LocalDate datePublication;
    private LocalDate dateLimite;
    private String type;
    private Integer pointsMax;
    private String fichierUrl;
    private String statut;
    private String anneeScolaire;
    private long totalSoumissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
