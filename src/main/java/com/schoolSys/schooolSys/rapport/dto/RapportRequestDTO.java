package com.schoolSys.schooolSys.rapport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RapportRequestDTO {

    @NotBlank(message = "Le titre est requis")
    private String titre;

    @NotBlank(message = "Le type est requis")
    private String type;

    private String periode;

    @NotBlank(message = "L'auteur est requis")
    private String auteur;

    private String statut;

    private String destinataire;

    private String description;

    private String fichier;
}
