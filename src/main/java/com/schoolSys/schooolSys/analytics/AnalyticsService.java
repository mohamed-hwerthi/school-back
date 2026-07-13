package com.schoolSys.schooolSys.analytics;

import java.util.UUID;

import com.schoolSys.schooolSys.absence.Absence;
import com.schoolSys.schooolSys.absence.AbsenceRepository;
import com.schoolSys.schooolSys.analytics.dto.*;
import com.schoolSys.schooolSys.discipline.Incident;
import com.schoolSys.schooolSys.discipline.IncidentRepository;
import com.schoolSys.schooolSys.finance.Paiement;
import com.schoolSys.schooolSys.finance.PaiementRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final NoteRepository noteRepository;
    private final AbsenceRepository absenceRepository;
    private final IncidentRepository incidentRepository;
    private final PaiementRepository paiementRepository;
    private final ClasseRepository classeRepository;
    private final KpiConfigRepository kpiConfigRepository;

    /**
     * Aggregate 360-degree view for a single student.
     */
    public SuiviEleveDTO getSuiviEleve(UUID eleveId) {
        Student student = studentRepository.findById(eleveId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", eleveId));

        // Grades per trimester
        List<Double> moyenneParTrimestre = new ArrayList<>();
        for (int trimestre = 1; trimestre <= 3; trimestre++) {
            List<Note> notes = noteRepository.findByStudentIdAndTrimestre(eleveId, trimestre);
            double avg = notes.stream()
                    .filter(n -> n.getValeur() != null)
                    .mapToDouble(Note::getValeur)
                    .average()
                    .orElse(0.0);
            moyenneParTrimestre.add(Math.round(avg * 100.0) / 100.0);
        }

        // Absences
        List<Absence> absences = absenceRepository.findByEleveId(eleveId);
        long totalAbsences = absences.stream().filter(a -> "ABSENCE".equals(a.getType())).count();
        long totalRetards = absences.stream().filter(a -> "RETARD".equals(a.getType())).count();

        // Incidents
        List<Incident> incidents = incidentRepository.findByEleveId(eleveId);
        long totalIncidents = incidents.size();

        // Payments
        List<Paiement> paiements = paiementRepository.findByStudentId(eleveId);
        long paye = paiements.stream().filter(p -> p.getStatut() == Paiement.StatutPaiement.PAYE).count();
        long total = paiements.size();
        String paiementsStatus = total == 0 ? "N/A"
                : paye == total ? "A_JOUR"
                : paye > 0 ? "PARTIEL"
                : "EN_RETARD";

        // Risk score (0-100)
        double scoreRisque = computeRiskScore(moyenneParTrimestre, totalAbsences, totalRetards, totalIncidents, paiementsStatus);
        String niveauRisque = scoreRisque < 25 ? "FAIBLE"
                : scoreRisque < 50 ? "MOYEN"
                : scoreRisque < 75 ? "ELEVE"
                : "CRITIQUE";

        return SuiviEleveDTO.builder()
                .eleveId(eleveId)
                .nom(student.getLastName())
                .prenom(student.getFirstName())
                .classe(student.getClasse())
                .moyenneParTrimestre(moyenneParTrimestre)
                .totalAbsences(totalAbsences)
                .totalRetards(totalRetards)
                .totalIncidents(totalIncidents)
                .paiementsStatus(paiementsStatus)
                .scoreRisque(scoreRisque)
                .niveauRisque(niveauRisque)
                .build();
    }

    /**
     * Returns students with risk score above the given threshold.
     */
    public List<SuiviEleveDTO> getElevesARisque(Double seuilScore) {
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(s -> getSuiviEleve(s.getId()))
                .filter(dto -> dto.getScoreRisque() >= seuilScore)
                .sorted(Comparator.comparingDouble(SuiviEleveDTO::getScoreRisque).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Compares all classes on key metrics.
     */
    public ComparaisonClassesDTO getComparaisonClasses() {
        List<Classe> classes = classeRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();

        List<ComparaisonClassesDTO.ClasseStats> statsList = new ArrayList<>();

        for (Classe classe : classes) {
            String classeName = classe.getNiveau().getName() + " " + classe.getLetter();
            List<Student> classeStudents = allStudents.stream()
                    .filter(s -> classeName.equalsIgnoreCase(s.getClasse()))
                    .toList();

            long effectif = classeStudents.size();
            if (effectif == 0) continue;

            // Average grade
            List<UUID> studentIds = classeStudents.stream().map(Student::getId).toList();
            double moyenne = noteRepository.findAll().stream()
                    .filter(n -> n.getValeur() != null && studentIds.contains(n.getStudent().getId()))
                    .mapToDouble(Note::getValeur)
                    .average()
                    .orElse(0.0);
            moyenne = Math.round(moyenne * 100.0) / 100.0;

            // Pass rate (>= 10)
            long totalNotes = noteRepository.findAll().stream()
                    .filter(n -> n.getValeur() != null && studentIds.contains(n.getStudent().getId()))
                    .count();
            long passedNotes = noteRepository.findAll().stream()
                    .filter(n -> n.getValeur() != null && studentIds.contains(n.getStudent().getId()) && n.getValeur() >= 10)
                    .count();
            double tauxReussite = totalNotes > 0
                    ? Math.round((double) passedNotes / totalNotes * 100.0 * 10.0) / 10.0
                    : 0.0;

            // Attendance rate
            long totalAbsences = studentIds.stream()
                    .mapToLong(id -> absenceRepository.findByEleveId(id).stream()
                            .filter(a -> "ABSENCE".equals(a.getType())).count())
                    .sum();
            double tauxPresence = effectif > 0
                    ? Math.round((1.0 - (double) totalAbsences / (effectif * 180)) * 100.0 * 10.0) / 10.0
                    : 100.0;
            tauxPresence = Math.max(0, Math.min(100, tauxPresence));

            statsList.add(ComparaisonClassesDTO.ClasseStats.builder()
                    .classeId(classe.getId())
                    .classeName(classeName)
                    .moyenne(moyenne)
                    .tauxReussite(tauxReussite)
                    .tauxPresence(tauxPresence)
                    .effectif(effectif)
                    .build());
        }

        return ComparaisonClassesDTO.builder().classes(statsList).build();
    }

    /**
     * Computes current KPI values versus configured targets.
     */
    public List<KpiDTO> getKpis() {
        List<KpiConfig> configs = kpiConfigRepository.findByActifTrue();
        List<KpiDTO> kpis = new ArrayList<>();

        for (KpiConfig config : configs) {
            BigDecimal valeurActuelle = computeKpiValue(config.getType());

            String statut;
            if (config.getSeuilAlerte() != null && valeurActuelle.compareTo(config.getSeuilAlerte()) < 0) {
                statut = "CRITIQUE";
            } else if (config.getValeurCible() != null && valeurActuelle.compareTo(config.getValeurCible()) < 0) {
                statut = "ALERTE";
            } else {
                statut = "OK";
            }

            kpis.add(KpiDTO.builder()
                    .nom(config.getNom())
                    .type(config.getType())
                    .valeurActuelle(valeurActuelle)
                    .valeurCible(config.getValeurCible())
                    .seuilAlerte(config.getSeuilAlerte())
                    .statut(statut)
                    .tendance("STABLE")
                    .build());
        }

        return kpis;
    }

    /**
     * Track cohort retention and performance across years for a given niveau.
     */
    public List<CohorteDTO> getCohortes(UUID niveauId) {
        List<Student> allStudents = studentRepository.findAll();

        // Group students by school year based on enrollment date
        Map<String, List<Student>> byYear = new LinkedHashMap<>();
        for (Student s : allStudents) {
            if (s.getEnrollmentDate() == null) continue;
            int year = s.getEnrollmentDate().getYear();
            int month = s.getEnrollmentDate().getMonthValue();
            String anneeScolaire = month >= 9
                    ? year + "-" + (year + 1)
                    : (year - 1) + "-" + year;
            byYear.computeIfAbsent(anneeScolaire, k -> new ArrayList<>()).add(s);
        }

        List<CohorteDTO> cohortes = new ArrayList<>();
        for (Map.Entry<String, List<Student>> entry : byYear.entrySet()) {
            List<Student> students = entry.getValue();
            long effectifInitial = students.size();
            long effectifFinal = students.stream()
                    .filter(s -> !"Radié".equalsIgnoreCase(s.getStatus()) && !"Inactif".equalsIgnoreCase(s.getStatus()))
                    .count();
            double tauxRetention = effectifInitial > 0
                    ? Math.round((double) effectifFinal / effectifInitial * 100.0 * 10.0) / 10.0
                    : 0.0;

            List<UUID> ids = students.stream().map(Student::getId).toList();
            double moyenneGenerale = noteRepository.findAll().stream()
                    .filter(n -> n.getValeur() != null && ids.contains(n.getStudent().getId()))
                    .mapToDouble(Note::getValeur)
                    .average()
                    .orElse(0.0);
            moyenneGenerale = Math.round(moyenneGenerale * 100.0) / 100.0;

            cohortes.add(CohorteDTO.builder()
                    .anneeScolaire(entry.getKey())
                    .effectifInitial(effectifInitial)
                    .effectifFinal(effectifFinal)
                    .tauxRetention(tauxRetention)
                    .moyenneGenerale(moyenneGenerale)
                    .build());
        }

        return cohortes;
    }

    // ── Private helpers ──

    private double computeRiskScore(List<Double> moyennes, long absences, long retards, long incidents, String paiementStatus) {
        double score = 0;

        // Academic risk (0-40 points)
        double avgGrade = moyennes.stream().filter(m -> m > 0).mapToDouble(Double::doubleValue).average().orElse(10.0);
        if (avgGrade < 8) score += 40;
        else if (avgGrade < 10) score += 30;
        else if (avgGrade < 12) score += 15;

        // Attendance risk (0-25 points)
        if (absences > 20) score += 25;
        else if (absences > 10) score += 15;
        else if (absences > 5) score += 8;
        score += Math.min(5, retards * 0.5);

        // Discipline risk (0-20 points)
        if (incidents > 5) score += 20;
        else if (incidents > 2) score += 12;
        else if (incidents > 0) score += 5;

        // Payment risk (0-15 points)
        if ("EN_RETARD".equals(paiementStatus)) score += 15;
        else if ("PARTIEL".equals(paiementStatus)) score += 8;

        return Math.min(100, Math.max(0, score));
    }

    private BigDecimal computeKpiValue(String type) {
        switch (type) {
            case "TAUX_REUSSITE": {
                List<Note> allNotes = noteRepository.findAll();
                long total = allNotes.stream().filter(n -> n.getValeur() != null).count();
                long passed = allNotes.stream().filter(n -> n.getValeur() != null && n.getValeur() >= 10).count();
                return total > 0
                        ? BigDecimal.valueOf(passed).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
            }
            case "TAUX_PRESENCE": {
                long totalStudents = studentRepository.count();
                long totalAbsences = absenceRepository.count();
                if (totalStudents == 0) return BigDecimal.valueOf(100);
                double rate = (1.0 - (double) totalAbsences / (totalStudents * 180)) * 100;
                return BigDecimal.valueOf(Math.max(0, Math.min(100, rate))).setScale(2, RoundingMode.HALF_UP);
            }
            case "TAUX_PAIEMENT": {
                String anneeScolaire = "2025-2026";
                BigDecimal totalDu = paiementRepository.sumMontantDuByAnneeScolaire(anneeScolaire);
                BigDecimal totalPaye = paiementRepository.sumMontantPayeByAnneeScolaire(anneeScolaire);
                return totalDu.compareTo(BigDecimal.ZERO) > 0
                        ? totalPaye.multiply(BigDecimal.valueOf(100)).divide(totalDu, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
            }
            case "MOYENNE_GENERALE": {
                double avg = noteRepository.findAll().stream()
                        .filter(n -> n.getValeur() != null)
                        .mapToDouble(Note::getValeur)
                        .average()
                        .orElse(0.0);
                return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
            }
            default:
                return BigDecimal.ZERO;
        }
    }
}
