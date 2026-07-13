package com.schoolSys.schooolSys.conseilclasse.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of a "conseil de classe": a student with their annual average and
 * the automatically proposed end-of-year decision (ANN-010 / ANN-011).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropositionPassageDTO {

    private UUID studentId;
    private String studentName;

    /** Current niveau name (matches {@code Niveau.name}). */
    private String niveauActuel;

    /** Current classe display name (e.g. "1A"). */
    private String classeActuelle;

    /** Per-trimestre general averages — {@code null} when the trimestre has no grades. */
    private Double moyenneT1;
    private Double moyenneT2;
    private Double moyenneT3;

    /** Mean of the available trimestre averages — {@code null} when no grades at all. */
    private Double moyenneAnnuelle;

    /** Annual rank in the class (1 = best); {@code null} when no annual average. */
    private Integer rang;

    /** Proposed decision: {@code PASSAGE} or {@code REDOUBLEMENT}. */
    private String decisionProposee;

    /** Next niveau name for a PASSAGE; {@code null} when none is configured. */
    private String niveauSuivant;

    /** Honour-roll mention derived from the annual average; {@code null} below the threshold (ANN-024). */
    private String mention;
}
