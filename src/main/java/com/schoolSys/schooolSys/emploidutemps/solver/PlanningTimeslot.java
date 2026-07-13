package com.schoolSys.schooolSys.emploidutemps.solver;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningTimeslot {

    private Long id;
    private UUID creneauId;
    private Integer jourSemaine; // 1=Monday ... 6=Saturday
    private LocalTime heureDebut;
    private LocalTime heureFin;

    /**
     * Position of this slot within its day (0-based).
     * Used by the consecutive-lesson constraint so the solver can compare
     * two slots without parsing times each time.
     */
    private Integer slotIndexInDay;

    public String getDayAndTime() {
        return jourSemaine + "-" + heureDebut;
    }
}
