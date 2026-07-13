package com.schoolSys.schooolSys.emploidutemps.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Read-only summary of whether the timetable can be generated for a given scope.
 * Returned by {@code GET /api/emploi-du-temps/preview-check}; consumed by the wizard's
 * step 1 to decide if the user is allowed to launch the solver.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetablePreviewCheckDTO {

    /** All metrics below are scoped to the requested niveau (or all niveaux if none). */
    private UUID anneeScolaireId;
    private String anneeScolaireLabel;
    private UUID niveauId;
    private String niveauName;

    // ── Modules / volumes ────────────────────────
    private Integer totalModules;
    private Integer modulesWithVolume;
    private Integer volumesWithoutTeacher;
    private Integer totalLessonsToSchedule;          // sum of nbHeuresHebdo

    // ── Teachers ────────────────────────────────
    private Integer totalTeachersInvolved;
    private Integer teachersWithoutDispos;            // teachers with no row in enseignant_disponibilites
    private List<TeacherSummary> teachersWithoutDisposList;  // names + ids — for the "Saisir maintenant" button

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherSummary {
        private UUID id;
        private String name;
    }

    // ── Rooms ────────────────────────────────────
    private Integer totalAvailableRooms;
    private Map<String, Integer> roomsByType;         // e.g. { NORMAL: 8, LABO_SVT: 1 }
    private List<String> missingRoomTypes;            // module-required types with zero rooms

    // ── Slots ────────────────────────────────────
    private Integer courseSlotsPerWeek;               // creneaux of type COURS × 6 days
    private Integer totalSlotCapacity;                // courseSlots × rooms

    // ── Verdict ──────────────────────────────────
    /** false → at least one blocker present; the wizard should disable the "Generate" button. */
    private Boolean canGenerate;

    /** Hard issues — block generation. */
    private List<String> blockers;

    /** Soft issues — generation will run but quality may degrade. */
    private List<String> warnings;
}
