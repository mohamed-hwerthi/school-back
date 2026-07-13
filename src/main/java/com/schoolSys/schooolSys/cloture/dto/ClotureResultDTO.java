package com.schoolSys.schooolSys.cloture.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Outcome of a school-year closure (ANN-030). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClotureResultDTO {

    private UUID anneeClotureeId;
    private String anneeClotureeLabel;

    /** Id/label of the year created during the closure; {@code null} when none. */
    private UUID nouvelleAnneeId;
    private String nouvelleAnneeLabel;
    private int trimestresCrees;

    private String message;
}
