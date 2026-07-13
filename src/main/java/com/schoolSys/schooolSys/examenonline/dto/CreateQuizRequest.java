package com.schoolSys.schooolSys.examenonline.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateQuizRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    private UUID moduleId;

    private UUID classeId;

    private UUID enseignantId;

    @NotNull(message = "La duree est obligatoire")
    @Builder.Default
    private Integer dureeMinutes = 60;

    @Builder.Default
    private BigDecimal noteTotale = new BigDecimal("20");

    @Builder.Default
    private Boolean melangerQuestions = false;

    @Builder.Default
    private Boolean melangerReponses = false;

    @Builder.Default
    private Boolean afficherResultats = true;

    @Builder.Default
    private Integer tentativesMax = 1;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @Builder.Default
    private String statut = "BROUILLON";
}
