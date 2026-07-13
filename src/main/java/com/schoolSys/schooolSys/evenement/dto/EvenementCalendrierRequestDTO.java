package com.schoolSys.schooolSys.evenement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EvenementCalendrierRequestDTO {

    @NotBlank(message = "Le titre est requis")
    private String titre;

    private String description;

    @NotNull(message = "La date de début est requise")
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @NotBlank(message = "Le type est requis")
    private String type;

    private String couleur;

    private String lieu;
}
