package com.schoolSys.schooolSys.anneescolaire.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnneeScolaireResponseDTO {
    private UUID id;
    private String label;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean active;
    private Boolean cloturee;
    private List<TrimestreDTO> trimestres;
    private List<VacanceDTO> vacances;
    private List<JourFerieDTO> joursFeries;
    private LocalDateTime createdAt;
}
