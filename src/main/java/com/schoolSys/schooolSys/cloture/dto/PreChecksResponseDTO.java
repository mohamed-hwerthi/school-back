package com.schoolSys.schooolSys.cloture.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Result of the pre-closure verifications for a school year (ANN-031). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreChecksResponseDTO {

    private UUID anneeId;
    private String anneeScolaire;

    /** {@code true} when no blocking check failed — the year can be closed. */
    private boolean cloturable;

    private List<PreCheckDTO> checks;
}
