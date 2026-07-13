package com.schoolSys.schooolSys.circulaire.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CirculaireRequestDTO {

    @NotBlank(message = "Le titre est requis")
    private String titre;

    @NotBlank(message = "Le type est requis")
    private String type;

    @NotBlank(message = "Le contenu est requis")
    private String contenu;

    @NotBlank(message = "L'auteur est requis")
    private String auteur;

    private String cible;

    private String pieceJointe;

    private Boolean important;
}
