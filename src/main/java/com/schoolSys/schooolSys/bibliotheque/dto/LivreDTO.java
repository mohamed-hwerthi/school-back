package com.schoolSys.schooolSys.bibliotheque.dto;

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
public class LivreDTO {

    private UUID id;
    private String titre;
    private String auteur;
    private String isbn;
    private String categorie;
    private String editeur;
    private Integer anneePublication;
    private String description;
    private Integer nombreExemplaires;
    private Integer exemplairesDisponibles;
    private String emplacement;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
