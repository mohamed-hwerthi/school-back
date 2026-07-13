package com.schoolSys.schooolSys.analytics.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SuiviEleveDTO {

    private UUID eleveId;
    private String nom;
    private String prenom;
    private String classe;
    private List<Double> moyenneParTrimestre;
    private long totalAbsences;
    private long totalRetards;
    private long totalIncidents;
    private String paiementsStatus;
    private double scoreRisque;
    private String niveauRisque;
}
