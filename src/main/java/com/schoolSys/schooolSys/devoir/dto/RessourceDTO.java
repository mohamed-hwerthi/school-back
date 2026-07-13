package com.schoolSys.schooolSys.devoir.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RessourceDTO {

    private UUID id;
    private String titre;
    private String description;
    private UUID moduleId;
    private String type;
    private String fichierUrl;
    private String lienExterne;
    private UUID enseignantId;
    private Long tailleFichier;
    private String anneeScolaire;
    private LocalDateTime createdAt;
}
