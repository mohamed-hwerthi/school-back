package com.schoolSys.schooolSys.conseilclasse.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of a "conseil de classe": the propositions for every student of a
 * class plus the context needed to validate them (ANN-010 / ANN-011).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConseilClasseDTO {

    private UUID classeId;
    private String classeNom;
    private String niveauNom;

    /** Next niveau name used to pre-fill a PASSAGE; {@code null} when none configured. */
    private String niveauSuivant;

    /** Pass threshold from the active barème (defaults to 10). */
    private Double seuilPassage;

    /** Active academic year label (e.g. "2025-2026"), stored on generated passages. */
    private String anneeScolaire;

    private List<PropositionPassageDTO> propositions;
}
