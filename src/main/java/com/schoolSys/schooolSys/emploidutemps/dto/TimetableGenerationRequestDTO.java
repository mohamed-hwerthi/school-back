package com.schoolSys.schooolSys.emploidutemps.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Two operating modes:
 * <ul>
 *   <li><b>Legacy mode</b> — caller provides {@code assignments} (one row per teacher × class × module
 *       with the weekly hour count) and {@code rooms} (free-text names). Used by the older UI flow.</li>
 *   <li><b>Auto mode</b> — caller leaves {@code assignments} empty (and optionally {@code rooms}).
 *       The service then loads the data from {@code module_classe_volume} for the active or specified
 *       école year, scoping to {@code niveauId} if provided. Rooms come from the {@code classrooms}
 *       table when {@code rooms} is empty.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableGenerationRequestDTO {

    /** Optional in auto mode (loaded from DB if empty). */
    @Valid
    private List<TeachingAssignmentDTO> assignments;

    /** Optional in auto mode (loaded from classrooms table if empty). */
    private List<String> rooms;

    /** Restrict generation to a single niveau (auto mode only). */
    private UUID niveauId;

    /** Defaults to the active année scolaire (auto mode only). */
    private UUID anneeScolaireId;

    @Builder.Default
    private Integer solverTimeoutSeconds = 30;
}
