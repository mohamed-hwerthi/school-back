package com.schoolSys.schooolSys.rh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateFormationRequest {
    @NotNull
    private String titre;
    private String description;
    private String formateur;
    @NotNull
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String lieu;
    private Integer nombreHeures;
    private BigDecimal cout;
    private String statut;
}
