package com.schoolSys.schooolSys.teacher;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.schoolSys.schooolSys.absence.Absence;
import com.schoolSys.schooolSys.absence.AbsenceRepository;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.devoir.Devoir;
import com.schoolSys.schooolSys.devoir.DevoirRepository;
import com.schoolSys.schooolSys.devoir.Soumission;
import com.schoolSys.schooolSys.devoir.SoumissionRepository;
import com.schoolSys.schooolSys.examen.Examen;
import com.schoolSys.schooolSys.examen.ExamenRepository;
import com.schoolSys.schooolSys.examenonline.Quiz;
import com.schoolSys.schooolSys.examenonline.QuizRepository;
import com.schoolSys.schooolSys.examenonline.Tentative;
import com.schoolSys.schooolSys.examenonline.TentativeRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.dto.TeacherHomeDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service dédié aux cartes du home enseignant — agrégations
 * légères qui scope sur les classes/modules de l'enseignant courant.
 */
@Service
@RequiredArgsConstructor
public class TeacherHomeService {

    private static final int CURRENT_TRIMESTRE = 1; // TODO: dériver de la date

    private final CurrentUserContext currentUser;
    private final DevoirRepository devoirRepository;
    private final SoumissionRepository soumissionRepository;
    private final QuizRepository quizRepository;
    private final TentativeRepository tentativeRepository;
    private final ExamenRepository examenRepository;
    private final NoteRepository noteRepository;
    private final AbsenceRepository absenceRepository;
    private final StudentRepository studentRepository;
    private final ClasseRepository classeRepository;

    public TeacherHomeDTO.PendingCorrections getPendingCorrections() {
        UUID teacherId = currentUser.getCurrentTeacher().map(Teacher::getId).orElse(null);
        if (teacherId == null) {
            return TeacherHomeDTO.PendingCorrections.builder().devoirs(0).quiz(0).build();
        }

        long devoirsCount = devoirRepository.findByEnseignantIdOrderByDateLimiteDesc(teacherId).stream()
                .filter(d -> Boolean.FALSE.equals(d.getDeleted()))
                .flatMap(d -> soumissionRepository.findByDevoirIdOrderByDateSoumissionDesc(d.getId()).stream())
                .filter(s -> Boolean.FALSE.equals(s.getDeleted()))
                .filter(s -> !Boolean.TRUE.equals(s.getCorrige()))
                .count();

        long quizCount = quizRepository.findByEnseignantIdOrderByCreatedAtDesc(teacherId).stream()
                .filter(q -> Boolean.FALSE.equals(q.getDeleted()))
                .flatMap(q -> tentativeRepository.findByQuizIdOrderByDateDebutDesc(q.getId()).stream())
                .filter(t -> "SOUMISE".equals(t.getStatut()))
                .count();

        return TeacherHomeDTO.PendingCorrections.builder()
                .devoirs((int) devoirsCount)
                .quiz((int) quizCount)
                .build();
    }

    public List<TeacherHomeDTO.StudentAtRisk> getStudentsAtRisk(int limit) {
        Set<UUID> studentIds = currentUser.getScopedStudentIdsForTeacher();
        if (studentIds.isEmpty()) return List.of();

        LocalDate monthAgo = LocalDate.now().minusDays(30);

        // Pré-charger les classes pour le label (Niveau + lettre)
        Map<String, String> classeLabelByKey = classeRepository.findAll().stream()
                .filter(c -> Boolean.FALSE.equals(c.getDeleted()))
                .collect(Collectors.toMap(
                        c -> c.getNiveau().getName() + "||" + c.getLetter(),
                        c -> c.getNiveau().getName() + " " + c.getLetter(),
                        (a, b) -> a));

        List<TeacherHomeDTO.StudentAtRisk> risks = studentIds.stream()
                .map(studentRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(s -> {
                    double moyenne = computeAverage(s.getId(), CURRENT_TRIMESTRE);
                    int absences = countRecentAbsences(s.getId(), monthAgo);
                    String motif;
                    double valeur;
                    if (moyenne > 0 && moyenne < 10) {
                        motif = "moyenne_faible";
                        valeur = moyenne;
                    } else if (absences > 5) {
                        motif = "absences_repetees";
                        valeur = absences;
                    } else {
                        return null; // pas à risque
                    }
                    String key = (s.getNiveau() != null ? s.getNiveau() : "") + "||" + (s.getClasse() != null ? s.getClasse() : "");
                    return TeacherHomeDTO.StudentAtRisk.builder()
                            .studentId(s.getId())
                            .prenom(s.getFirstName())
                            .nom(s.getLastName())
                            .classeNom(classeLabelByKey.getOrDefault(key, s.getNiveau() + " " + s.getClasse()))
                            .motif(motif)
                            .valeur(valeur)
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparingDouble(r ->
                        "moyenne_faible".equals(r.getMotif()) ? r.getValeur() : -r.getValeur()))
                .limit(limit)
                .collect(Collectors.toList());

        return risks;
    }

    public TeacherHomeDTO.PendingTasks getPendingTasks() {
        UUID teacherId = currentUser.getCurrentTeacher().map(Teacher::getId).orElse(null);
        if (teacherId == null) {
            return TeacherHomeDTO.PendingTasks.builder().items(List.of()).total(0).build();
        }

        LocalDate today = LocalDate.now();

        // Examens dont la date limite de saisie est passée et qui n'ont aucune note
        List<TeacherHomeDTO.PendingTask> items = examenRepository.findFiltered(null, null, null, null).stream()
                .filter(e -> Boolean.FALSE.equals(e.getDeleted()))
                .filter(e -> e.getTeacher() != null && teacherId.equals(e.getTeacher().getId()))
                .filter(e -> e.getDateLimiteSaisie() != null && e.getDateLimiteSaisie().isBefore(today))
                .filter(e -> noteRepository.countByExamenIdAndTrimestre(e.getId(), e.getTrimestre()) == 0)
                .sorted(Comparator.comparing(Examen::getDateLimiteSaisie))
                .map(e -> TeacherHomeDTO.PendingTask.builder()
                        .kind("NOTE")
                        .date(e.getDateLimiteSaisie())
                        .classeNom(buildClasseLabel(e.getClasse()))
                        .moduleNom(e.getModule() != null ? e.getModule().getName() : null)
                        .label("Notes à saisir — " + (e.getName() != null ? e.getName() : "Examen"))
                        .build())
                .collect(Collectors.toList());

        return TeacherHomeDTO.PendingTasks.builder()
                .items(items.stream().limit(10).collect(Collectors.toList()))
                .total(items.size())
                .build();
    }

    private double computeAverage(UUID studentId, int trimestre) {
        List<Note> notes = noteRepository.findByStudentIdAndTrimestre(studentId, trimestre);
        return notes.stream()
                .filter(n -> n.getValeur() != null)
                .mapToDouble(n -> n.getValeur().doubleValue())
                .average()
                .orElse(0.0);
    }

    private int countRecentAbsences(UUID studentId, LocalDate since) {
        return (int) absenceRepository.findByEleveId(studentId).stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .filter(a -> "ABSENCE".equals(a.getType()))
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(since))
                .count();
    }

    private String buildClasseLabel(Classe c) {
        if (c == null) return null;
        return (c.getNiveau() != null ? c.getNiveau().getName() : "") + " " + c.getLetter();
    }
}
