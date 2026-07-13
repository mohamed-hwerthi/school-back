package com.schoolSys.schooolSys.scolarite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Re-enrolment tracking for a school year vs the previous one (ANN-051). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuiviReinscriptionDTO {

    private String anneeScolaire;
    private String anneePrecedente;

    /** Total students enrolled this year. */
    private int totalInscrits;

    /** Enrolled this year and the previous year. */
    private int reinscrits;

    /** Enrolled this year but not the previous year. */
    private int nouveaux;

    /** Enrolled the previous year but not this year. */
    private int partis;
}
