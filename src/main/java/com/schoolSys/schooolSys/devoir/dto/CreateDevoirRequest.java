package com.schoolSys.schooolSys.devoir.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDevoirRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    private UUID moduleId;

    private UUID classeId;

    private UUID enseignantId;

    private LocalDate datePublication;

    @NotNull(message = "La date limite est obligatoire")
    private LocalDate dateLimite;

    @Builder.Default
    private String type = "DEVOIR";

    @Builder.Default
    private Integer pointsMax = 20;

    private String fichierUrl;

    @Builder.Default
    private String statut = "PUBLIE";

    private String anneeScolaire;
}
