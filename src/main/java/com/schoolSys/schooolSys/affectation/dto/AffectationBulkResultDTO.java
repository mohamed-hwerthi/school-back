package com.schoolSys.schooolSys.affectation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Outcome of a bulk assignment: how many rows were created, how many were
 * skipped (already existing), and the created rows themselves.
 */
@Data
@Builder
public class AffectationBulkResultDTO {
    private int created;
    private int skipped;
    private List<AffectationDTO> affectations;
}
