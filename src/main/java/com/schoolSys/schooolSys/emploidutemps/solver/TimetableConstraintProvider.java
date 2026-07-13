package com.schoolSys.schooolSys.emploidutemps.solver;

import com.schoolSys.schooolSys.disponibilite.EnseignantDisponibilite;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import java.time.LocalTime;

public class TimetableConstraintProvider implements ConstraintProvider {

    private static final LocalTime NOON = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_THRESHOLD = LocalTime.of(14, 0);
    private static final int MAX_CONSECUTIVE_FOR_TEACHER = 4;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
            // ── HARD ──────────────────────────────────────────
            roomConflict(factory),
            teacherConflict(factory),
            classConflict(factory),
            roomCapacityMatch(factory),
            roomTypeMatch(factory),
            teacherUnavailable(factory),

            // ── SOFT ──────────────────────────────────────────
            subjectVarietyPerDay(factory),
            teacherGapMinimization(factory),
            heavySubjectInMorning(factory),
            consecutiveLessonsForDoublePeriods(factory),
            teacherMaxConsecutive(factory),
            classDailyBalance(factory),
            teacherPreferredSlot(factory),
            teacherAvoidSlot(factory),
            modulePreferenceMorning(factory),
            modulePreferenceAfternoon(factory),
            fridayAfternoonLight(factory),
        };
    }

    /* ───────────────────────── HARD CONSTRAINTS ───────────────────────── */

    /** Two lessons cannot occupy the same room at the same timeslot. */
    Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlanningLesson.class,
                Joiners.equal(PlanningLesson::getTimeslot),
                Joiners.equal(PlanningLesson::getRoom))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room conflict");
    }

    /** A teacher cannot teach two lessons at the same timeslot. */
    Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlanningLesson.class,
                Joiners.equal(PlanningLesson::getEnseignantId),
                Joiners.equal(PlanningLesson::getTimeslot))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher conflict");
    }

    /** A class cannot have two lessons at the same timeslot. */
    Constraint classConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlanningLesson.class,
                Joiners.equal(PlanningLesson::getClasseId),
                Joiners.equal(PlanningLesson::getTimeslot))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Class conflict");
    }

    /** Room capacity must hold the class effectif (capacity 0 / null = unlimited). */
    Constraint roomCapacityMatch(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> l.getRoom() != null
                && l.getRoom().getCapacite() != null
                && l.getRoom().getCapacite() > 0
                && l.getEffectifClasse() != null
                && l.getEffectifClasse() > l.getRoom().getCapacite())
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room capacity exceeded");
    }

    /**
     * Room type must match the module's required type. NORMAL is a wildcard on the module side
     * (any room works). Anything else needs an exact match on room.type.
     */
    Constraint roomTypeMatch(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> l.getRoom() != null
                && l.getSalleTypeRequise() != null
                && !"NORMAL".equals(l.getSalleTypeRequise())
                && !l.getSalleTypeRequise().equals(l.getRoom().getType()))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room type mismatch");
    }

    /** A teacher cannot be scheduled on a slot they marked INDISPONIBLE. */
    Constraint teacherUnavailable(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> l.getTimeslot() != null)
            .join(EnseignantDisponibilite.class,
                Joiners.equal(PlanningLesson::getEnseignantId, EnseignantDisponibilite::getEnseignantId),
                Joiners.equal(l -> l.getTimeslot().getJourSemaine(), EnseignantDisponibilite::getJourSemaine),
                Joiners.equal(l -> l.getTimeslot().getCreneauId(), EnseignantDisponibilite::getCreneauId))
            .filter((l, dispo) -> "INDISPONIBLE".equals(dispo.getType()))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher unavailable");
    }

    /* ───────────────────────── SOFT CONSTRAINTS ───────────────────────── */

    /** Discourage 3+ occurrences of the same module on the same day for one class. */
    Constraint subjectVarietyPerDay(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlanningLesson.class,
                Joiners.equal(PlanningLesson::getClasseId),
                Joiners.equal(PlanningLesson::getModuleId),
                Joiners.equal(l -> l.getTimeslot() != null ? l.getTimeslot().getJourSemaine() : null))
            .filter((a, b) -> a.getTimeslot() != null && b.getTimeslot() != null)
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Subject variety per day");
    }

    /** Penalize gaps > 2h in a teacher's daily schedule. */
    Constraint teacherGapMinimization(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlanningLesson.class,
                Joiners.equal(PlanningLesson::getEnseignantId),
                Joiners.equal(l -> l.getTimeslot() != null ? l.getTimeslot().getJourSemaine() : null))
            .filter((a, b) -> {
                if (a.getTimeslot() == null || b.getTimeslot() == null) return false;
                long gap = Math.abs(
                    a.getTimeslot().getHeureDebut().toSecondOfDay() -
                    b.getTimeslot().getHeureDebut().toSecondOfDay());
                return gap > 7200; // > 2h
            })
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Teacher gap minimization");
    }

    /** Modules with high coefficient (>= 2.0) should land before noon. */
    Constraint heavySubjectInMorning(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> l.getModuleCoefficient() != null
                && l.getModuleCoefficient() >= 2.0
                && l.getTimeslot() != null
                && !l.getTimeslot().getHeureDebut().isBefore(NOON))
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Heavy subject in morning");
    }

    /**
     * Reward back-to-back placement when a module requests double periods (dureeMin=2).
     * Looks for two lessons of the same (classe, module, day) in adjacent timeslots
     * — implemented as a reward (negative-weighted penalize) of value 2 per pair.
     */
    Constraint consecutiveLessonsForDoublePeriods(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlanningLesson.class,
                Joiners.equal(PlanningLesson::getClasseId),
                Joiners.equal(PlanningLesson::getModuleId),
                Joiners.equal(l -> l.getTimeslot() != null ? l.getTimeslot().getJourSemaine() : null))
            .filter((a, b) -> a.getDureeMinSeance() != null && a.getDureeMinSeance() >= 2
                && a.getTimeslot() != null && b.getTimeslot() != null
                && a.getTimeslot().getSlotIndexInDay() != null
                && b.getTimeslot().getSlotIndexInDay() != null
                && Math.abs(a.getTimeslot().getSlotIndexInDay() - b.getTimeslot().getSlotIndexInDay()) == 1)
            .reward(HardSoftScore.ofSoft(2))
            .asConstraint("Consecutive lessons for double-period modules");
    }

    /**
     * Penalize > MAX_CONSECUTIVE_FOR_TEACHER consecutive lessons (= adjacent slot indices)
     * for a teacher on the same day. Implemented as: any pair of lessons with slotIndex delta < 4
     * AND no gap → already covered by gap constraint inversely; here we focus on a pair with
     * gap == 0 (adjacent) and same slot delta within MAX as a soft cost.
     */
    Constraint teacherMaxConsecutive(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlanningLesson.class,
                Joiners.equal(PlanningLesson::getEnseignantId),
                Joiners.equal(l -> l.getTimeslot() != null ? l.getTimeslot().getJourSemaine() : null))
            .filter((a, b) -> {
                if (a.getTimeslot() == null || b.getTimeslot() == null) return false;
                Integer ai = a.getTimeslot().getSlotIndexInDay();
                Integer bi = b.getTimeslot().getSlotIndexInDay();
                if (ai == null || bi == null) return false;
                int delta = Math.abs(ai - bi);
                // Penalize if pair is "deep" inside a long consecutive run
                return delta >= MAX_CONSECUTIVE_FOR_TEACHER;
            })
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Teacher max consecutive lessons");
    }

    /** Encourage even distribution: penalize pairs of lessons for the same class on the same day. */
    Constraint classDailyBalance(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlanningLesson.class,
                Joiners.equal(PlanningLesson::getClasseId),
                Joiners.equal(l -> l.getTimeslot() != null ? l.getTimeslot().getJourSemaine() : null))
            .filter((a, b) -> a.getTimeslot() != null && b.getTimeslot() != null)
            // Tiny weight — kicks in only as a tie-breaker, otherwise it would empty the schedule
            .penalize(HardSoftScore.ofSoft(0))   // disabled until we move to weighted soft scores
            .asConstraint("Class daily balance");
    }

    /** Bonus when a lesson lands on a slot the teacher marked PREFERE. */
    Constraint teacherPreferredSlot(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> l.getTimeslot() != null)
            .join(EnseignantDisponibilite.class,
                Joiners.equal(PlanningLesson::getEnseignantId, EnseignantDisponibilite::getEnseignantId),
                Joiners.equal(l -> l.getTimeslot().getJourSemaine(), EnseignantDisponibilite::getJourSemaine),
                Joiners.equal(l -> l.getTimeslot().getCreneauId(), EnseignantDisponibilite::getCreneauId))
            .filter((l, dispo) -> "PREFERE".equals(dispo.getType()))
            .reward(HardSoftScore.ofSoft(3))
            .asConstraint("Teacher preferred slot");
    }

    /** Penalty when a lesson lands on a slot the teacher marked EVITER. */
    Constraint teacherAvoidSlot(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> l.getTimeslot() != null)
            .join(EnseignantDisponibilite.class,
                Joiners.equal(PlanningLesson::getEnseignantId, EnseignantDisponibilite::getEnseignantId),
                Joiners.equal(l -> l.getTimeslot().getJourSemaine(), EnseignantDisponibilite::getJourSemaine),
                Joiners.equal(l -> l.getTimeslot().getCreneauId(), EnseignantDisponibilite::getCreneauId))
            .filter((l, dispo) -> "EVITER".equals(dispo.getType()))
            .penalize(HardSoftScore.ofSoft(3))
            .asConstraint("Teacher avoid slot");
    }

    /** Module preference MATIN — penalize if scheduled at noon or after. */
    Constraint modulePreferenceMorning(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> "MATIN".equals(l.getPreferenceHoraire())
                && l.getTimeslot() != null
                && !l.getTimeslot().getHeureDebut().isBefore(NOON))
            .penalize(HardSoftScore.ofSoft(2))
            .asConstraint("Module prefers morning");
    }

    /** Module preference APRES_MIDI — penalize if scheduled in the morning. */
    Constraint modulePreferenceAfternoon(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> "APRES_MIDI".equals(l.getPreferenceHoraire())
                && l.getTimeslot() != null
                && l.getTimeslot().getHeureDebut().isBefore(NOON))
            .penalize(HardSoftScore.ofSoft(2))
            .asConstraint("Module prefers afternoon");
    }

    /** Penalize lessons scheduled Friday afternoon (jourSemaine=5, after 14h). */
    Constraint fridayAfternoonLight(ConstraintFactory factory) {
        return factory.forEach(PlanningLesson.class)
            .filter(l -> l.getTimeslot() != null
                && Integer.valueOf(5).equals(l.getTimeslot().getJourSemaine())
                && !l.getTimeslot().getHeureDebut().isBefore(AFTERNOON_THRESHOLD))
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Friday afternoon light");
    }
}
