package com.schoolSys.schooolSys.scolarite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Outcome of a mass re-enrolment of the passing students (ANN-050). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReinscriptionResultDTO {

    private String anneeSource;
    private String anneeDestination;

    /** Students re-enrolled into the destination year. */
    private int nbReinscrits;

    /** Students already enrolled in the destination year (skipped). */
    private int nbIgnores;

    private String message;
}
