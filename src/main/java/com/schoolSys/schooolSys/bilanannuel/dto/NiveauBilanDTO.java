package com.schoolSys.schooolSys.bilanannuel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** End-of-year decision counts for a single niveau (ANN-021). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NiveauBilanDTO {

    private String niveau;
    private int total;
    private int nbPassage;
    private int nbRedoublement;
    private int nbExclusion;
    private int nbTransfert;

    /** Passage rate for this niveau (passages / total, %). */
    private double tauxPassage;
}
