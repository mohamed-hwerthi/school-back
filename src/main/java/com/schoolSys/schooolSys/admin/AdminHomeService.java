package com.schoolSys.schooolSys.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.schoolSys.schooolSys.absence.AbsenceRepository;
import com.schoolSys.schooolSys.admin.dto.AdminHomeDTO;
import com.schoolSys.schooolSys.admin.dto.TeacherPerformanceDTO;
import com.schoolSys.schooolSys.affectation.AffectationRepository;
import com.schoolSys.schooolSys.devoir.DevoirRepository;
import com.schoolSys.schooolSys.examen.Examen;
import com.schoolSys.schooolSys.examen.ExamenRepository;
import com.schoolSys.schooolSys.finance.Paiement;
import com.schoolSys.schooolSys.finance.PaiementRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Agrégations rapides pour le home admin (KPI cards + alertes).
 * Conçu pour un seul appel mobile — pas de pagination, payload <2KB.
 */
@Service
@RequiredArgsConstructor
public class AdminHomeService {

    private static final int CAPACITE_CLASSE_THEORIQUE = 35; // configurable plus tard
    private static final int CURRENT_TRIMESTRE = 1; // TODO: dériver de la date

    private final StudentRepository studentRepository;
    private final ClasseRepository classeRepository;
    private final AffectationRepository affectationRepository;
    private final PaiementRepository paiementRepository;
    private final AbsenceRepository absenceRepository;
    private final NoteRepository noteRepository;
    private final TeacherRepository teacherRepository;
    private final ExamenRepository examenRepository;
    private final DevoirRepository devoirRepository;

    public AdminHomeDTO.DashboardKpis getDashboardKpis(String anneeScolaire) {
        List<Student> activeStudents = studentRepository.findAll().stream()
                .filter(s -> Boolean.FALSE.equals(s.getDeleted()))
                .filter(s -> !"Inactif".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());

        long effectif = activeStudents.size();

        // Taux d'occupation moyen
        List<Classe> classes = classeRepository.findAll().stream()
                .filter(c -> Boolean.FALSE.equals(c.getDeleted()))
                .collect(Collectors.toList());
        double tauxOccupation = 0.0;
        if (!classes.isEmpty()) {
            // group students by "niveau||letter" key to find classe size
            Map<String, Long> sizeByKey = activeStudents.stream()
                    .filter(s -> s.getNiveau() != null && s.getClasse() != null)
                    .collect(Collectors.groupingBy(s -> s.getNiveau() + "||" + s.getClasse(), Collectors.counting()));
            double avgOcc = classes.stream()
                    .mapToDouble(c -> {
                        String key = (c.getNiveau() != null ? c.getNiveau().getName() : "") + "||" + c.getLetter();
                        long size = sizeByKey.getOrDefault(key, 0L);
                        return Math.min(100.0, (size * 100.0) / CAPACITE_CLASSE_THEORIQUE);
                    })
                    .average()
                    .orElse(0.0);
            tauxOccupation = Math.round(avgOcc * 10.0) / 10.0;
        }

        // CA du mois en cours
        String moisCourant = String.format("%02d", YearMonth.now().getMonthValue());
        BigDecimal caDuMois = paiementRepository.findByMoisAndAnneeScolaire(moisCourant, anneeScolaire).stream()
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                .map(Paiement::getMontantPaye)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Impayés (montantDu - montantPaye pour les non PAYE)
        BigDecimal impayes = paiementRepository.findByAnneeScolaire(anneeScolaire).stream()
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                .filter(p -> p.getStatut() != Paiement.StatutPaiement.PAYE)
                .map(p -> p.getMontantDu().subtract(p.getMontantPaye() != null ? p.getMontantPaye() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Taux de présence — 100 - (absences / élèves / jours_école_30j) en %
        LocalDate monthAgo = LocalDate.now().minusDays(30);
        long absencesCount = absenceRepository.findAll().stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .filter(a -> "ABSENCE".equals(a.getType()))
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(monthAgo))
                .count();
        // Hypothèse : 20 jours d'école sur 30 jours civils
        double expectedPresences = effectif * 20.0;
        double tauxPresence = expectedPresences > 0
                ? Math.max(0.0, Math.round((100.0 - (absencesCount * 100.0 / expectedPresences)) * 10.0) / 10.0)
                : 0.0;

        // Taux de réussite global — % d'élèves avec moyenne >= 10 ce trimestre
        long withNotes = 0;
        long withPassing = 0;
        for (Student s : activeStudents) {
            List<Note> notes = noteRepository.findByStudentIdAndTrimestreAndAnneeScolaire(s.getId(), CURRENT_TRIMESTRE, anneeScolaire);
            if (notes.isEmpty()) continue;
            double avg = notes.stream()
                    .filter(n -> n.getValeur() != null)
                    .mapToDouble(n -> n.getValeur().doubleValue())
                    .average()
                    .orElse(0.0);
            if (avg == 0.0) continue;
            withNotes++;
            if (avg >= 10.0) withPassing++;
        }
        double tauxReussite = withNotes > 0
                ? Math.round((withPassing * 100.0 / withNotes) * 10.0) / 10.0
                : 0.0;

        return AdminHomeDTO.DashboardKpis.builder()
                .effectifTotal(effectif)
                .tauxOccupation(tauxOccupation)
                .caDuMois(caDuMois.setScale(2, RoundingMode.HALF_UP))
                .impayes(impayes.setScale(2, RoundingMode.HALF_UP))
                .tauxPresence(tauxPresence)
                .tauxReussite(tauxReussite)
                .build();
    }

    /**
     * MOB-FUNC-033 — tableau de performance enseignants (toutes affectations années courantes).
     */
    public List<TeacherPerformanceDTO> getTeachersPerformance(String anneeScolaire, int trimestre) {
        return teacherRepository.findAll().stream()
                .filter(t -> Boolean.FALSE.equals(t.getDeleted()))
                .map(t -> buildPerformance(t, anneeScolaire, trimestre))
                .sorted((a, b) -> Double.compare(
                        b.getSaisiesAJour() != null ? b.getSaisiesAJour() : 0.0,
                        a.getSaisiesAJour() != null ? a.getSaisiesAJour() : 0.0))
                .collect(Collectors.toList());
    }

    private TeacherPerformanceDTO buildPerformance(Teacher t, String anneeScolaire, int trimestre) {
        int nbClasses = (int) affectationRepository
                .findByTeacherIdAndAnneeScolaire(t.getId(), anneeScolaire).stream()
                .map(a -> a.getClasseId())
                .distinct()
                .count();

        List<Examen> examens = examenRepository.findFiltered(null, null, trimestre, anneeScolaire).stream()
                .filter(e -> Boolean.FALSE.equals(e.getDeleted()))
                .filter(e -> e.getTeacher() != null && t.getId().equals(e.getTeacher().getId()))
                .collect(Collectors.toList());

        double saisiesAJour = 0.0;
        if (!examens.isEmpty()) {
            long withNotes = examens.stream()
                    .filter(e -> noteRepository.countByExamenIdAndTrimestreAndAnneeScolaire(e.getId(), e.getTrimestre(), anneeScolaire) > 0)
                    .count();
            saisiesAJour = Math.round((withNotes * 100.0 / examens.size()) * 10.0) / 10.0;
        }

        double avg = examens.stream()
                .flatMap(e -> noteRepository.findByExamenIdAndTrimestreAndAnneeScolaire(e.getId(), e.getTrimestre(), anneeScolaire).stream())
                .filter(n -> n.getValeur() != null)
                .mapToDouble(n -> n.getValeur().doubleValue())
                .average()
                .orElse(0.0);

        int nbDevoirs = (int) devoirRepository.findByEnseignantIdOrderByDateLimiteDesc(t.getId()).stream()
                .filter(d -> Boolean.FALSE.equals(d.getDeleted()))
                .count();

        return TeacherPerformanceDTO.builder()
                .teacherId(t.getId())
                .prenom(t.getFirstName())
                .nom(t.getLastName())
                .nbClasses(nbClasses)
                .saisiesAJour(saisiesAJour)
                .moyenneDonnee(Math.round(avg * 10.0) / 10.0)
                .nbDevoirs(nbDevoirs)
                .build();
    }

    public AdminHomeDTO.OperationalAlerts getOperationalAlerts(String anneeScolaire) {
        LocalDate monthAgo = LocalDate.now().minusDays(30);

        // Impayés en retard depuis plus de 30 jours
        int impayes30j = (int) paiementRepository.findByAnneeScolaire(anneeScolaire).stream()
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                .filter(p -> p.getStatut() == Paiement.StatutPaiement.EN_RETARD)
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().toLocalDate().isBefore(monthAgo))
                .count();

        // Élèves avec plus de 10 absences ce mois
        Map<UUID, Long> absencesByStudent = absenceRepository.findAll().stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .filter(a -> "ABSENCE".equals(a.getType()))
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(monthAgo))
                .collect(Collectors.groupingBy(a -> a.getEleveId(), Collectors.counting()));
        int absenteesCeMois = (int) absencesByStudent.values().stream().filter(c -> c > 10).count();

        // Classes sans aucune affectation pour l'année courante
        java.util.Set<UUID> classesAvecAffectation = affectationRepository.findByAnneeScolaireOrderByTeacherIdAsc(anneeScolaire).stream()
                .map(a -> a.getClasseId())
                .collect(Collectors.toSet());
        int classesSansAffectation = (int) classeRepository.findAll().stream()
                .filter(c -> Boolean.FALSE.equals(c.getDeleted()))
                .filter(c -> !classesAvecAffectation.contains(c.getId()))
                .count();

        return AdminHomeDTO.OperationalAlerts.builder()
                .impayes30j(impayes30j)
                .absenteesCeMois(absenteesCeMois)
                .classesSansAffectation(classesSansAffectation)
                .build();
    }
}
