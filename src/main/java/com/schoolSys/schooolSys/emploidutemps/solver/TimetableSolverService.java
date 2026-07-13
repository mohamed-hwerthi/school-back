package com.schoolSys.schooolSys.emploidutemps.solver;

import java.util.UUID;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import com.schoolSys.schooolSys.classroom.Classroom;
import com.schoolSys.schooolSys.classroom.ClassroomRepository;
import com.schoolSys.schooolSys.disponibilite.EnseignantDisponibilite;
import com.schoolSys.schooolSys.disponibilite.EnseignantDisponibiliteRepository;
import com.schoolSys.schooolSys.emploidutemps.Creneau;
import com.schoolSys.schooolSys.emploidutemps.CreneauRepository;
import com.schoolSys.schooolSys.emploidutemps.EmploiDuTemps;
import com.schoolSys.schooolSys.emploidutemps.EmploiDuTempsRepository;
import com.schoolSys.schooolSys.emploidutemps.dto.*;
import com.schoolSys.schooolSys.module.Module;
import com.schoolSys.schooolSys.module.ModuleRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import com.schoolSys.schooolSys.volumehoraire.ModuleClasseVolume;
import com.schoolSys.schooolSys.volumehoraire.ModuleClasseVolumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableSolverService {

    private final CreneauRepository creneauRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleClasseVolumeRepository volumeRepository;
    private final EnseignantDisponibiliteRepository disponibiliteRepository;
    private final ClassroomRepository classroomRepository;
    private final ClasseRepository classeRepository;
    private final NiveauRepository niveauRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;

    @Transactional
    public TimetableGenerationResponseDTO generate(TimetableGenerationRequestDTO request) {
        // 1. Load course timeslots from DB (filter PAUSE/RECREATION)
        List<Creneau> courseCreneaux = creneauRepository.findByType("COURS");
        if (courseCreneaux.isEmpty()) {
            return infeasible("Aucun creneau de type COURS trouve en base de donnees");
        }
        // Sort to assign a stable slotIndexInDay
        courseCreneaux.sort(Comparator.comparing(Creneau::getHeureDebut));

        // 2. Resolve which assignments to use: legacy (from request) or auto (from DB)
        List<TeachingAssignmentDTO> assignments = resolveAssignments(request);
        if (assignments.isEmpty()) {
            return infeasible("Aucune affectation à planifier (saisis le volume horaire dans la page dédiée)");
        }

        // 3. Build PlanningTimeslot list (creneau × jour 1..6)
        List<PlanningTimeslot> timeslots = new ArrayList<>();
        long timeslotIdSeq = 1;
        for (int jour = 1; jour <= 6; jour++) {
            int idx = 0;
            for (Creneau c : courseCreneaux) {
                timeslots.add(PlanningTimeslot.builder()
                    .id(timeslotIdSeq++)
                    .creneauId(c.getId())
                    .jourSemaine(jour)
                    .heureDebut(c.getHeureDebut())
                    .heureFin(c.getHeureFin())
                    .slotIndexInDay(idx++)
                    .build());
            }
        }

        // 4. Build PlanningRoom list — auto mode reads from classrooms, legacy uses provided strings
        List<PlanningRoom> rooms = resolveRooms(request);
        if (rooms.isEmpty()) {
            return infeasible("Aucune salle disponible (ajoute des salles dans la page Salles)");
        }

        // 5. Pre-load module attributes & class size lookups
        Set<UUID> moduleIds = assignments.stream()
            .map(TeachingAssignmentDTO::getModuleId).collect(Collectors.toSet());
        Map<UUID, Module> moduleById = moduleRepository.findAllById(moduleIds).stream()
            .collect(Collectors.toMap(Module::getId, m -> m));

        Set<UUID> classeIds = assignments.stream()
            .map(TeachingAssignmentDTO::getClasseId).collect(Collectors.toSet());
        Map<UUID, Integer> effectifByClasseId = computeEffectifByClasse(classeIds);

        // 6. Build PlanningLesson list — one per (assignment × hour)
        List<PlanningLesson> lessons = new ArrayList<>();
        long lessonIdSeq = 1;
        for (TeachingAssignmentDTO a : assignments) {
            Module mod = moduleById.get(a.getModuleId());
            Double coeff = mod != null ? mod.getCoeffEtatique() : 1.0;
            String salleType = mod != null ? mod.getSalleTypeRequise() : "NORMAL";
            Integer dureeMin = mod != null ? mod.getDureeMinSeance() : 1;
            String prefHoraire = mod != null ? mod.getPreferenceHoraire() : "INDIFFERENT";
            Integer effectif = effectifByClasseId.getOrDefault(a.getClasseId(), 0);

            for (int h = 0; h < a.getNbHeures(); h++) {
                lessons.add(PlanningLesson.builder()
                    .id(lessonIdSeq++)
                    .classeId(a.getClasseId())
                    .moduleId(a.getModuleId())
                    .enseignantId(a.getEnseignantId())
                    .moduleCoefficient(coeff)
                    .effectifClasse(effectif)
                    .salleTypeRequise(salleType)
                    .dureeMinSeance(dureeMin)
                    .preferenceHoraire(prefHoraire)
                    .sequenceInModule(h)
                    .build());
            }
        }

        // 7. Capacity check — does the (timeslot × room) grid hold every lesson?
        int totalSlots = timeslots.size() * rooms.size();
        if (lessons.size() > totalSlots) {
            return infeasible(String.format(
                "Pas assez de creneaux disponibles: %d cours a placer pour %d creneaux × %d salles = %d possibilites",
                lessons.size(), timeslots.size(), rooms.size(), totalSlots));
        }

        // 8. Load teacher unavailabilities into the constraint context (problem facts on the solution)
        Set<UUID> enseignantIds = assignments.stream()
            .map(TeachingAssignmentDTO::getEnseignantId).collect(Collectors.toSet());
        List<EnseignantDisponibilite> dispos = enseignantIds.isEmpty()
            ? List.of()
            : disponibiliteRepository.findAll().stream()
                .filter(d -> enseignantIds.contains(d.getEnseignantId()))
                .toList();

        TimetableSolution problem = new TimetableSolution(timeslots, rooms, lessons);
        problem.setDisponibilites(dispos);

        // 9. Solve
        int timeout = request.getSolverTimeoutSeconds() != null ? request.getSolverTimeoutSeconds() : 30;
        SolverConfig solverConfig = new SolverConfig()
            .withSolutionClass(TimetableSolution.class)
            .withEntityClasses(PlanningLesson.class)
            .withConstraintProviderClass(TimetableConstraintProvider.class)
            .withTerminationConfig(new TerminationConfig()
                .withSpentLimit(Duration.ofSeconds(timeout)));

        Solver<TimetableSolution> solver = SolverFactory.<TimetableSolution>create(solverConfig).buildSolver();

        log.info("Starting timetable solving with {} lessons, {} timeslots, {} rooms, {} dispos, timeout={}s",
            lessons.size(), timeslots.size(), rooms.size(), dispos.size(), timeout);

        TimetableSolution solution = solver.solve(problem);
        log.info("Timetable solving completed with score: {}", solution.getScore());

        // 10. Persist results: wipe existing entries for affected classes, save new ones
        List<String> unresolvedConflicts = new ArrayList<>();
        if (solution.getScore().hardScore() < 0) {
            unresolvedConflicts.add(String.format(
                "Score dur negatif (%d): il reste des conflits non resolus (capacite, dispo prof, ou type de salle)",
                solution.getScore().hardScore()));
        }

        for (UUID classeId : classeIds) {
            emploiDuTempsRepository.deleteByClasseId(classeId);
        }

        List<EmploiDuTemps> entitiesToSave = new ArrayList<>();
        for (PlanningLesson lesson : solution.getLessons()) {
            if (lesson.getTimeslot() == null || lesson.getRoom() == null) {
                unresolvedConflicts.add(String.format(
                    "Cours non place: classeId=%d, moduleId=%d, enseignantId=%d",
                    lesson.getClasseId(), lesson.getModuleId(), lesson.getEnseignantId()));
                continue;
            }
            entitiesToSave.add(EmploiDuTemps.builder()
                .classeId(lesson.getClasseId())
                .creneauId(lesson.getTimeslot().getCreneauId())
                .jourSemaine(lesson.getTimeslot().getJourSemaine())
                .moduleId(lesson.getModuleId())
                .enseignantId(lesson.getEnseignantId())
                .salle(lesson.getRoom().getName())
                .classroomId(lesson.getRoom().getClassroomId())
                .build());
        }
        List<EmploiDuTemps> saved = emploiDuTempsRepository.saveAll(entitiesToSave);

        // 11. Map to response DTOs
        List<EmploiDuTempsResponseDTO> responseDtos = saved.stream()
            .map(e -> EmploiDuTempsResponseDTO.builder()
                .id(e.getId())
                .classeId(e.getClasseId())
                .creneauId(e.getCreneauId())
                .jourSemaine(e.getJourSemaine())
                .moduleId(e.getModuleId())
                .enseignantId(e.getEnseignantId())
                .salle(e.getSalle())
                .classroomId(e.getClassroomId())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build())
            .collect(Collectors.toList());

        String status = solution.getScore().hardScore() >= 0 ? "SOLVED" : "INFEASIBLE";
        return TimetableGenerationResponseDTO.builder()
            .status(status)
            .score(solution.getScore().toShortString())
            .entries(responseDtos)
            .unresolvedConflicts(unresolvedConflicts)
            .build();
    }

    /* ─── Helpers ─────────────────────────────────────────── */

    private List<TeachingAssignmentDTO> resolveAssignments(TimetableGenerationRequestDTO request) {
        if (request.getAssignments() != null && !request.getAssignments().isEmpty()) {
            // Legacy explicit mode
            return request.getAssignments();
        }
        // Auto mode: pull volume horaire for the resolved année
        UUID anneeId = request.getAnneeScolaireId();
        if (anneeId == null) {
            anneeId = anneeScolaireRepository.findByActiveTrue()
                .map(a -> a.getId())
                .orElse(null);
        }
        if (anneeId == null) {
            return List.of();
        }

        List<ModuleClasseVolume> volumes = volumeRepository.findByAnneeScolaireId(anneeId);

        // Optional niveau filter — only keep volumes whose classe belongs to the niveau
        if (request.getNiveauId() != null) {
            Set<UUID> classesInNiveau = classeRepository.findByNiveauId(request.getNiveauId())
                .stream().map(Classe::getId).collect(Collectors.toSet());
            volumes = volumes.stream()
                .filter(v -> classesInNiveau.contains(v.getClasseId()))
                .toList();
        }

        return volumes.stream()
            .filter(v -> v.getEnseignantId() != null)  // need a teacher to schedule
            .map(v -> TeachingAssignmentDTO.builder()
                .classeId(v.getClasseId())
                .moduleId(v.getModuleId())
                .enseignantId(v.getEnseignantId())
                .nbHeures(v.getNbHeuresHebdo())
                .build())
            .toList();
    }

    private List<PlanningRoom> resolveRooms(TimetableGenerationRequestDTO request) {
        // Legacy: explicit list of room name strings (capacity unknown → unlimited)
        if (request.getRooms() != null && !request.getRooms().isEmpty()) {
            List<PlanningRoom> rooms = new ArrayList<>();
            for (int i = 0; i < request.getRooms().size(); i++) {
                rooms.add(PlanningRoom.builder()
                    .id((long) (i + 1))
                    .name(request.getRooms().get(i))
                    .type("NORMAL")
                    .build());
            }
            return rooms;
        }

        // Auto: load from classrooms table (only those marked Disponible)
        List<Classroom> available = classroomRepository.findAll().stream()
            .filter(c -> c.getStatus() == null || "Disponible".equalsIgnoreCase(c.getStatus()))
            .toList();
        List<PlanningRoom> rooms = new ArrayList<>();
        long roomIdSeq = 1;
        for (Classroom c : available) {
            rooms.add(PlanningRoom.builder()
                .id(roomIdSeq++)
                .name(c.getName())
                .classroomId(c.getId())
                .capacite(c.getCapacity())
                .type(c.getType())
                .build());
        }
        return rooms;
    }

    /**
     * Build a (classeId → studentCount) map. We rely on the fullName scheme used by the rest of
     * the app (digits of niveau name + class letter, e.g. "1ère année A" → "1A").
     */
    private Map<UUID, Integer> computeEffectifByClasse(Set<UUID> classeIds) {
        Map<UUID, Integer> result = new HashMap<>();
        for (Classe c : classeRepository.findAllById(classeIds)) {
            String fullName = buildFullName(c);
            long count = studentRepository.countByClasse(fullName);
            result.put(c.getId(), (int) count);
        }
        return result;
    }

    private static String formatTeacherName(Teacher t) {
        String first = t.getFirstName() != null ? t.getFirstName() : "";
        String last = t.getLastName() != null ? t.getLastName() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "#" + t.getId() : full;
    }

    private static String buildFullName(Classe c) {
        StringBuilder digits = new StringBuilder();
        for (char ch : c.getNiveau().getName().toCharArray()) {
            if (Character.isDigit(ch)) digits.append(ch);
            else break;
        }
        return digits.toString() + c.getLetter();
    }

    private static TimetableGenerationResponseDTO infeasible(String message) {
        return TimetableGenerationResponseDTO.builder()
            .status("INFEASIBLE")
            .score("N/A")
            .entries(Collections.emptyList())
            .unresolvedConflicts(List.of(message))
            .build();
    }

    /* ─── Preview / pre-flight check ──────────────────────── */

    /**
     * Inspects the data without launching the solver and returns a structured report
     * describing what would happen if {@link #generate} were called now.
     */
    @Transactional(readOnly = true)
    public TimetablePreviewCheckDTO previewCheck(UUID niveauId, UUID anneeScolaireId) {
        List<String> blockers = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 1. Resolve année scolaire (active by default)
        var anneeOpt = anneeScolaireId != null
            ? anneeScolaireRepository.findById(anneeScolaireId)
            : anneeScolaireRepository.findByActiveTrue();
        if (anneeOpt.isEmpty()) {
            blockers.add("Aucune année scolaire active. Créez et activez une année dans Pédagogie › Année scolaire.");
            return TimetablePreviewCheckDTO.builder()
                .niveauId(niveauId)
                .canGenerate(false)
                .blockers(blockers)
                .warnings(warnings)
                .build();
        }
        var annee = anneeOpt.get();

        // 2. Resolve niveau (optional)
        String niveauName = null;
        if (niveauId != null) {
            var niv = niveauRepository.findById(niveauId).orElse(null);
            if (niv == null) {
                blockers.add("Niveau introuvable (id=" + niveauId + ")");
                return TimetablePreviewCheckDTO.builder()
                    .anneeScolaireId(annee.getId()).anneeScolaireLabel(annee.getLabel())
                    .niveauId(niveauId).canGenerate(false)
                    .blockers(blockers).warnings(warnings)
                    .build();
            }
            niveauName = niv.getName();
        }

        // 3. Modules in scope
        List<Module> modules = niveauId != null
            ? moduleRepository.findByNiveauIdOrderByOrdreEtatiqueAsc(niveauId)
            : moduleRepository.findAll();

        // 4. Volumes for the année (filtered to niveau via classes lookup if needed)
        List<ModuleClasseVolume> volumes = volumeRepository.findByAnneeScolaireId(annee.getId());
        if (niveauId != null) {
            Set<UUID> classesInNiveau = classeRepository.findByNiveauId(niveauId)
                .stream().map(Classe::getId).collect(Collectors.toSet());
            volumes = volumes.stream()
                .filter(v -> classesInNiveau.contains(v.getClasseId()))
                .toList();
        }

        Set<UUID> moduleIdsWithVolume = volumes.stream()
            .map(ModuleClasseVolume::getModuleId).collect(Collectors.toSet());
        int modulesWithVolume = (int) modules.stream()
            .filter(m -> moduleIdsWithVolume.contains(m.getId())).count();

        int volumesWithoutTeacher = (int) volumes.stream()
            .filter(v -> v.getEnseignantId() == null).count();

        int totalLessons = volumes.stream()
            .filter(v -> v.getEnseignantId() != null)
            .mapToInt(ModuleClasseVolume::getNbHeuresHebdo).sum();

        if (modules.isEmpty()) {
            blockers.add("Aucun module à planifier" + (niveauId != null ? " pour ce niveau" : ""));
        } else if (modulesWithVolume == 0) {
            blockers.add("Aucun module n'a de volume horaire saisi. Ouvrez Pédagogie › Volume horaire.");
        } else if (modulesWithVolume < modules.size()) {
            warnings.add(String.format("%d module(s) sans volume horaire — ces matières ne seront pas planifiées",
                modules.size() - modulesWithVolume));
        }
        if (volumesWithoutTeacher > 0) {
            warnings.add(String.format("%d ligne(s) de volume horaire sans enseignant attribué — exclues de la planification",
                volumesWithoutTeacher));
        }
        if (totalLessons == 0 && blockers.isEmpty()) {
            blockers.add("Volume horaire effectif à 0 (aucune ligne avec enseignant). Affectez les enseignants dans Volume horaire.");
        }

        // 5. Teachers — those involved in the volumes
        Set<UUID> teacherIds = volumes.stream()
            .map(ModuleClasseVolume::getEnseignantId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Set<UUID> teacherIdsWithDispos = teacherIds.isEmpty()
            ? Collections.emptySet()
            : disponibiliteRepository.findAll().stream()
                .map(EnseignantDisponibilite::getEnseignantId)
                .filter(teacherIds::contains)
                .collect(Collectors.toSet());
        Set<UUID> teacherIdsWithoutDispos = new HashSet<>(teacherIds);
        teacherIdsWithoutDispos.removeAll(teacherIdsWithDispos);
        List<TimetablePreviewCheckDTO.TeacherSummary> teachersWithoutDisposList =
            teacherIdsWithoutDispos.isEmpty()
                ? Collections.emptyList()
                : teacherRepository.findAllById(teacherIdsWithoutDispos).stream()
                    .map(t -> TimetablePreviewCheckDTO.TeacherSummary.builder()
                        .id(t.getId())
                        .name(formatTeacherName(t))
                        .build())
                    .sorted(Comparator.comparing(TimetablePreviewCheckDTO.TeacherSummary::getName))
                    .toList();
        int teachersWithoutDispos = teacherIdsWithoutDispos.size();
        if (teachersWithoutDispos > 0) {
            warnings.add(String.format("%d enseignant(s) sans disponibilités saisies — considérés disponibles 100%%",
                teachersWithoutDispos));
        }

        // 6. Rooms — count by type, and check required types are covered
        List<Classroom> availableRooms = classroomRepository.findAll().stream()
            .filter(c -> c.getStatus() == null || "Disponible".equalsIgnoreCase(c.getStatus()))
            .toList();
        Map<String, Integer> roomsByType = new HashMap<>();
        for (var room : availableRooms) {
            String type = room.getType() != null ? room.getType() : "NORMAL";
            roomsByType.merge(type, 1, Integer::sum);
        }

        Set<String> requiredTypes = modules.stream()
            .filter(m -> moduleIdsWithVolume.contains(m.getId()))
            .map(Module::getSalleTypeRequise)
            .filter(t -> t != null && !"NORMAL".equals(t))
            .collect(Collectors.toSet());
        List<String> missingTypes = requiredTypes.stream()
            .filter(t -> roomsByType.getOrDefault(t, 0) == 0)
            .sorted().toList();
        if (!missingTypes.isEmpty()) {
            blockers.add("Aucune salle disponible pour les types : " + String.join(", ", missingTypes));
        }
        if (availableRooms.isEmpty()) {
            blockers.add("Aucune salle disponible. Ajoutez/activez des salles dans Pédagogie › Salles.");
        }

        // 7. Slot capacity
        int courseCreneaux = creneauRepository.findByType("COURS").size();
        int courseSlotsPerWeek = courseCreneaux * 6;
        int totalSlotCapacity = courseSlotsPerWeek * Math.max(availableRooms.size(), 1);
        if (totalLessons > totalSlotCapacity) {
            blockers.add(String.format(
                "Capacité insuffisante : %d cours à placer pour %d créneaux × %d salles = %d possibilités",
                totalLessons, courseSlotsPerWeek, availableRooms.size(), totalSlotCapacity));
        } else if (totalLessons > totalSlotCapacity * 0.85) {
            warnings.add(String.format(
                "Charge à %d %% — la planification risque d'être rigide",
                Math.round(100.0 * totalLessons / totalSlotCapacity)));
        }

        return TimetablePreviewCheckDTO.builder()
            .anneeScolaireId(annee.getId())
            .anneeScolaireLabel(annee.getLabel())
            .niveauId(niveauId)
            .niveauName(niveauName)
            .totalModules(modules.size())
            .modulesWithVolume(modulesWithVolume)
            .volumesWithoutTeacher(volumesWithoutTeacher)
            .totalLessonsToSchedule(totalLessons)
            .totalTeachersInvolved(teacherIds.size())
            .teachersWithoutDispos(teachersWithoutDispos)
            .teachersWithoutDisposList(teachersWithoutDisposList)
            .totalAvailableRooms(availableRooms.size())
            .roomsByType(roomsByType)
            .missingRoomTypes(missingTypes)
            .courseSlotsPerWeek(courseSlotsPerWeek)
            .totalSlotCapacity(totalSlotCapacity)
            .canGenerate(blockers.isEmpty())
            .blockers(blockers)
            .warnings(warnings)
            .build();
    }
}
