package com.schoolSys.schooolSys.rapport.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RapportResponseDTO {

    private UUID id;
    private String titre;
    private String type;
    private String periode;
    private LocalDateTime dateGeneration;
    private String statut;
    private String auteur;
    private String destinataire;
    private String description;
    private String fichier;
}
