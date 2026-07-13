package com.schoolSys.schooolSys.evenement.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EvenementCalendrierResponseDTO {

    private UUID id;
    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String type;
    private String couleur;
    private String lieu;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
