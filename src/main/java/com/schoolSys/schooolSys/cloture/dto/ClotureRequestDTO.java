package com.schoolSys.schooolSys.cloture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Options for closing a school year (ANN-030 / ANN-033). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClotureRequestDTO {

    /** Create the next school year as part of the closure. */
    private boolean creerAnneeSuivante;

    private String labelAnneeSuivante;
    private LocalDate dateDebutSuivante;
    private LocalDate dateFinSuivante;

    /** Activate the newly created year (deactivates all others). */
    private boolean activerAnneeSuivante;
}
