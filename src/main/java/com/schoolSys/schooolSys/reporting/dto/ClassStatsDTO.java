package com.schoolSys.schooolSys.reporting.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * Per-class summary stats for a given trimestre — used by the admin "Stats école" view.
 */
@Data
@Builder
public class ClassStatsDTO {
    private UUID classeId;
    private String classeName;
    private String niveauName;
    private long nbEleves;

    /** Pondered average over all notes of the trimestre (0..20). 0 when no notes. */
    private double moyenne;

    /** Fraction of notes >= 10/20 (0..100). 0 when no notes. */
    private double tauxReussite;

    /** Fraction of present sessions over expected (0..100). 100 when no absences. */
    private double tauxPresence;

    private long totalAbsences;
    private long totalRetards;
    private long totalAbsencesJustifiees;
}
