package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;

import com.schoolSys.schooolSys.emploidutemps.solver.*;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimetableConstraintProviderTest {

    private static final LocalTime T08 = LocalTime.of(8, 0);
    private static final LocalTime T09 = LocalTime.of(9, 0);
    private static final LocalTime T10 = LocalTime.of(10, 0);
    private static final LocalTime T11 = LocalTime.of(11, 0);
    private static final LocalTime T14 = LocalTime.of(14, 0);
    private static final LocalTime T15 = LocalTime.of(15, 0);

    // ── Builders ──────────────────────────────────────────────
    // PlanningLesson/Timeslot/Room carry solver-tuning fields (capacity, room
    // type, preferences…) that are irrelevant to the conflict tests below.
    // These helpers set only the facts the hard constraints rely on; the rest
    // stay null — and every hard constraint is null-guarded.

    private PlanningTimeslot timeslot(Long id, UUID creneauId, Integer jourSemaine,
                                      LocalTime heureDebut, LocalTime heureFin) {
        return PlanningTimeslot.builder()
            .id(id).creneauId(creneauId).jourSemaine(jourSemaine)
            .heureDebut(heureDebut).heureFin(heureFin)
            .build();
    }

    private PlanningRoom room(Long id, String name) {
        return PlanningRoom.builder().id(id).name(name).build();
    }

    private PlanningLesson lesson(Long id, UUID classeId, UUID moduleId,
                                  UUID enseignantId, Double moduleCoefficient) {
        return PlanningLesson.builder()
            .id(id).classeId(classeId).moduleId(moduleId)
            .enseignantId(enseignantId).moduleCoefficient(moduleCoefficient)
            .build();
    }

    private TimetableSolution solve(TimetableSolution problem, int timeoutSeconds) {
        SolverConfig config = new SolverConfig()
            .withSolutionClass(TimetableSolution.class)
            .withEntityClasses(PlanningLesson.class)
            .withConstraintProviderClass(TimetableConstraintProvider.class)
            .withTerminationConfig(new TerminationConfig()
                .withSpentLimit(Duration.ofSeconds(timeoutSeconds)));

        SolverFactory<TimetableSolution> factory = SolverFactory.create(config);
        Solver<TimetableSolution> solver = factory.buildSolver();
        return solver.solve(problem);
    }

    @Test
    void solverFindsValidSolution_noHardViolations() {
        // 2 days x 3 creneaux = 6 timeslots
        List<PlanningTimeslot> timeslots = List.of(
            timeslot(1L, new UUID(0, 1), 1, T08, T09),
            timeslot(2L, new UUID(0, 2), 1, T09, T10),
            timeslot(3L, new UUID(0, 3), 1, T10, T11),
            timeslot(4L, new UUID(0, 1), 2, T08, T09),
            timeslot(5L, new UUID(0, 2), 2, T09, T10),
            timeslot(6L, new UUID(0, 3), 2, T10, T11)
        );

        List<PlanningRoom> rooms = List.of(
            room(1L, "Salle A1"),
            room(2L, "Salle A2")
        );

        // 4 lessons: 2 classes, 2 modules, 2 teachers
        List<PlanningLesson> lessons = new ArrayList<>();
        lessons.add(lesson(1L, new UUID(0, 1), new UUID(0, 10), new UUID(0, 100), 2.0));
        lessons.add(lesson(2L, new UUID(0, 1), new UUID(0, 11), new UUID(0, 101), 1.0));
        lessons.add(lesson(3L, new UUID(0, 2), new UUID(0, 10), new UUID(0, 100), 2.0));
        lessons.add(lesson(4L, new UUID(0, 2), new UUID(0, 11), new UUID(0, 102), 1.0));

        TimetableSolution problem = new TimetableSolution(timeslots, rooms, lessons);
        TimetableSolution solution = solve(problem, 5);

        assertNotNull(solution.getScore());
        assertTrue(solution.getScore().hardScore() >= 0,
            "Solver should find a solution with no hard constraint violations, but got: " + solution.getScore());

        // Verify all lessons are assigned
        for (PlanningLesson lesson : solution.getLessons()) {
            assertNotNull(lesson.getTimeslot(), "Every lesson should have a timeslot assigned");
            assertNotNull(lesson.getRoom(), "Every lesson should have a room assigned");
        }
    }

    @Test
    void solverRespects_roomConflict() {
        // 1 timeslot, 1 room, 2 lessons → impossible to avoid room conflict
        List<PlanningTimeslot> timeslots = List.of(
            timeslot(1L, new UUID(0, 1), 1, T08, T09)
        );
        List<PlanningRoom> rooms = List.of(
            room(1L, "Salle A1")
        );
        List<PlanningLesson> lessons = new ArrayList<>();
        lessons.add(lesson(1L, new UUID(0, 1), new UUID(0, 10), new UUID(0, 100), 1.0));
        lessons.add(lesson(2L, new UUID(0, 2), new UUID(0, 11), new UUID(0, 101), 1.0));

        TimetableSolution problem = new TimetableSolution(timeslots, rooms, lessons);
        TimetableSolution solution = solve(problem, 5);

        // With 1 timeslot and 1 room, 2 lessons must conflict
        assertTrue(solution.getScore().hardScore() < 0,
            "With only 1 timeslot and 1 room, 2 lessons must cause a hard constraint violation");
    }

    @Test
    void solverRespects_teacherConflict() {
        // 2 timeslots, 2 rooms, but same teacher for both lessons
        // Solver should find a solution by assigning different timeslots
        List<PlanningTimeslot> timeslots = List.of(
            timeslot(1L, new UUID(0, 1), 1, T08, T09),
            timeslot(2L, new UUID(0, 2), 1, T09, T10)
        );
        List<PlanningRoom> rooms = List.of(
            room(1L, "Salle A1"),
            room(2L, "Salle A2")
        );
        // Same teacher (100) for both lessons
        List<PlanningLesson> lessons = new ArrayList<>();
        lessons.add(lesson(1L, new UUID(0, 1), new UUID(0, 10), new UUID(0, 100), 1.0));
        lessons.add(lesson(2L, new UUID(0, 2), new UUID(0, 11), new UUID(0, 100), 1.0));

        TimetableSolution problem = new TimetableSolution(timeslots, rooms, lessons);
        TimetableSolution solution = solve(problem, 5);

        assertTrue(solution.getScore().hardScore() >= 0,
            "Solver should separate same-teacher lessons into different timeslots");

        // Verify different timeslots for same teacher
        PlanningLesson l1 = solution.getLessons().get(0);
        PlanningLesson l2 = solution.getLessons().get(1);
        assertNotEquals(l1.getTimeslot(), l2.getTimeslot(),
            "Same teacher should not be in the same timeslot");
    }

    @Test
    void solverRespects_classConflict() {
        // 2 timeslots, 2 rooms, same class for both lessons
        List<PlanningTimeslot> timeslots = List.of(
            timeslot(1L, new UUID(0, 1), 1, T08, T09),
            timeslot(2L, new UUID(0, 2), 1, T09, T10)
        );
        List<PlanningRoom> rooms = List.of(
            room(1L, "Salle A1"),
            room(2L, "Salle A2")
        );
        // Same class (new UUID(0, 1)) for both lessons
        List<PlanningLesson> lessons = new ArrayList<>();
        lessons.add(lesson(1L, new UUID(0, 1), new UUID(0, 10), new UUID(0, 100), 1.0));
        lessons.add(lesson(2L, new UUID(0, 1), new UUID(0, 11), new UUID(0, 101), 1.0));

        TimetableSolution problem = new TimetableSolution(timeslots, rooms, lessons);
        TimetableSolution solution = solve(problem, 5);

        assertTrue(solution.getScore().hardScore() >= 0,
            "Solver should separate same-class lessons into different timeslots");

        PlanningLesson l1 = solution.getLessons().get(0);
        PlanningLesson l2 = solution.getLessons().get(1);
        assertNotEquals(l1.getTimeslot(), l2.getTimeslot(),
            "Same class should not have two lessons at the same timeslot");
    }
}
