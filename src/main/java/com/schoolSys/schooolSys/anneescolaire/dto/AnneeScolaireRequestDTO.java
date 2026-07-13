package com.schoolSys.schooolSys.anneescolaire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AnneeScolaireRequestDTO {
    @NotBlank
    private String label;
    @NotNull
    private LocalDate dateDebut;
    @NotNull
    private LocalDate dateFin;
    private Boolean active;
    private List<TrimestreDTO> trimestres;
    private List<VacanceDTO> vacances;
    private List<JourFerieDTO> joursFeries;
}
