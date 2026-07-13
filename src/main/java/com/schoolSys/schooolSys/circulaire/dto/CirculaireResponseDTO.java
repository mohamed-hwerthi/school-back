package com.schoolSys.schooolSys.circulaire.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CirculaireResponseDTO {

    private UUID id;
    private String titre;
    private String type;
    private String contenu;
    private LocalDateTime dateCreation;
    private LocalDateTime datePublication;
    private String statut;
    private String auteur;
    private String cible;
    private String pieceJointe;
    private Boolean important;
}
