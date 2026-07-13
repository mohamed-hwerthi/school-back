package com.schoolSys.schooolSys.emploidutemps.solver;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningLesson {

    @PlanningId
    private Long id;

    // Fixed facts
    private UUID classeId;
    private UUID moduleId;
    private UUID enseignantId;
    private Double moduleCoefficient;

    // Solver-relevant module attributes
    private Integer effectifClasse;        // for room capacity check
    private String salleTypeRequise;       // for room type match
    private Integer dureeMinSeance;        // 1 or 2 (for double-period preference)
    private String preferenceHoraire;      // MATIN, APRES_MIDI, INDIFFERENT

    // Sequence index inside (classe, module) — used by the consecutive-double constraint
    // so the solver knows lesson #1 and #2 of "Maths in 6A" should be back-to-back.
    private Integer sequenceInModule;

    // Planning variables (assigned by solver)
    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    private PlanningTimeslot timeslot;

    @PlanningVariable(valueRangeProviderRefs = "roomRange")
    private PlanningRoom room;
}
