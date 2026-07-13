package com.schoolSys.schooolSys.scolarite.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Certificate of success for a student for a given school year (ANN-042). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttestationReussiteDTO {

    private UUID studentId;
    private String studentName;
    private String anneeScolaire;

    /** {@code true} when the student has a PASSAGE decision for that year. */
    private boolean eligible;

    private String ancienNiveau;
    private String nouveauNiveau;
    private String decision;
    private String message;
}
