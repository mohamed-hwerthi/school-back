package com.schoolSys.schooolSys.bibliotheque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLivreRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String auteur;
    private String isbn;
    private String categorie;
    private String editeur;
    private Integer anneePublication;
    private String description;

    @Positive(message = "Le nombre d'exemplaires doit etre positif")
    private Integer nombreExemplaires;

    private String emplacement;
    private String imageUrl;
}
