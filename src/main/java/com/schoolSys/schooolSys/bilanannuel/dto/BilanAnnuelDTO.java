package com.schoolSys.schooolSys.bilanannuel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Annual review: end-of-year decision counts and rates for a school year,
 * globally and broken down by niveau (ANN-020 / ANN-021).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BilanAnnuelDTO {

    private String anneeScolaire;

    /** Total number of recorded end-of-year decisions. */
    private int totalDecisions;

    private int nbPassage;
    private int nbRedoublement;
    private int nbExclusion;
    private int nbTransfert;

    /** Rates as a percentage of {@code totalDecisions}. */
    private double tauxPassage;
    private double tauxRedoublement;
    private double tauxExclusion;
    private double tauxTransfert;

    private List<NiveauBilanDTO> parNiveau;
}
