package com.schoolSys.schooolSys.emploidutemps.solver;

import com.schoolSys.schooolSys.disponibilite.EnseignantDisponibilite;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Collections;
import java.util.List;

@PlanningSolution
@Data
@NoArgsConstructor
public class TimetableSolution {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    private List<PlanningTimeslot> timeslots;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "roomRange")
    private List<PlanningRoom> rooms;

    /** Teacher unavailability/preference rows; consumed by constraints, not assigned by solver. */
    @ProblemFactCollectionProperty
    private List<EnseignantDisponibilite> disponibilites = Collections.emptyList();

    @PlanningEntityCollectionProperty
    private List<PlanningLesson> lessons;

    @PlanningScore
    private HardSoftScore score;

    public TimetableSolution(List<PlanningTimeslot> timeslots, List<PlanningRoom> rooms, List<PlanningLesson> lessons) {
        this.timeslots = timeslots;
        this.rooms = rooms;
        this.lessons = lessons;
    }
}
