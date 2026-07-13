package com.schoolSys.schooolSys.reporting;

import java.util.UUID;

import com.schoolSys.schooolSys.absence.Absence;
import com.schoolSys.schooolSys.absence.AbsenceRepository;
import com.schoolSys.schooolSys.evenement.EvenementCalendrier;
import com.schoolSys.schooolSys.evenement.EvenementCalendrierRepository;
import com.schoolSys.schooolSys.finance.PaiementRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.niveau.Niveau;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.reporting.dto.ClassDrillDownDTO;
import com.schoolSys.schooolSys.reporting.dto.ClassStatsDTO;
import com.schoolSys.schooolSys.reporting.dto.DashboardStatsDTO;
import com.schoolSys.schooolSys.reporting.dto.DayAttendanceDTO;
import com.schoolSys.schooolSys.reporting.dto.MonthlyTrendDTO;
import com.schoolSys.schooolSys.reporting.dto.RecentStudentDTO;
import com.schoolSys.schooolSys.reporting.dto.UpcomingEventDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingService {

    private final NoteRepository noteRepository;
    private final PaiementRepository paiementRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClasseRepository classeRepository;
    private final AbsenceRepository absenceRepository;
    private final EvenementCalendrierRepository evenementRepository;

    public DashboardStatsDTO getDashboardStats(String anneeScolaire) {
        long totalStudents = studentRepository.count();
        long totalTeachers = teacherRepository.count();
        long totalClasses = classeRepository.count();

        // Payment stats
        BigDecimal totalRevenue = paiementRepository.sumMontantPayeByAnneeScolaire(anneeScolaire);
        BigDecimal totalDu = paiementRepository.sumMontantDuByAnneeScolaire(anneeScolaire);
        BigDecimal totalPending = paiementRepository.sumImpayes(anneeScolaire);
        BigDecimal tauxRecouvrement = totalDu.compareTo(BigDecimal.ZERO) > 0
                ? totalRevenue.multiply(BigDecimal.valueOf(100)).divide(totalDu, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Absence rate
        long totalAbsences = absenceRepository.count();
        double tauxAbsence = totalStudents > 0
                ? Math.round((double) totalAbsences / totalStudents * 100.0 * 10.0) / 10.0
                : 0;

        // Average grade
        double moyenneGenerale = noteRepository.findAll().stream()
                .filter(n -> n.getValeur() != null)
                .mapToDouble(n -> n.getValeur())
                .average()
                .orElse(0);
        moyenneGenerale = Math.round(moyenneGenerale * 10.0) / 10.0;

        // Students by niveau
        List<Student> allStudents = studentRepository.findAll();
        Map<String, Long> studentsByNiveau = allStudents.stream()
                .filter(s -> s.getNiveau() != null && !s.getNiveau().isBlank())
                .collect(Collectors.groupingBy(Student::getNiveau, Collectors.counting()));

        // ── Banner counters ─────────────────────────────────────────
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);

        List<Absence> allAbsences = absenceRepository.findAll();
        long absencesToday = allAbsences.stream()
                .filter(a -> "ABSENCE".equalsIgnoreCase(a.getType()))
                .filter(a -> today.equals(a.getDate()))
                .count();

        long newEnrollmentsThisMonth = allStudents.stream()
                .filter(s -> s.getEnrollmentDate() != null
                        && YearMonth.from(s.getEnrollmentDate()).equals(thisMonth))
                .count();

        List<EvenementCalendrier> allEvents = evenementRepository.findAllByOrderByDateDebutAsc();
        long eventsThisMonth = allEvents.stream()
                .filter(e -> e.getDateDebut() != null
                        && YearMonth.from(e.getDateDebut()).equals(thisMonth))
                .count();

        // ── Weekly attendance: last 5 weekdays (Mon-Fri) ────────────
        List<DayAttendanceDTO> weeklyAttendance = buildWeeklyAttendance(today, allAbsences, totalStudents);

        // ── Upcoming events (next 5 from today) ─────────────────────
        List<UpcomingEventDTO> upcomingEvents = allEvents.stream()
                .filter(e -> e.getDateDebut() != null && !e.getDateDebut().isBefore(today))
                .limit(5)
                .map(e -> UpcomingEventDTO.builder()
                        .id(e.getId())
                        .titre(e.getTitre())
                        .dateDebut(e.getDateDebut())
                        .couleur(e.getCouleur())
                        .type(e.getType())
                        .lieu(e.getLieu())
                        .build())
                .toList();

        // ── Recent students (last 6 by enrollmentDate desc) ─────────
        List<RecentStudentDTO> recentStudents = allStudents.stream()
                .filter(s -> s.getEnrollmentDate() != null)
                .sorted(Comparator.comparing(Student::getEnrollmentDate).reversed())
                .limit(6)
                .map(s -> RecentStudentDTO.builder()
                        .id(s.getId())
                        .fullName((s.getFirstName() == null ? "" : s.getFirstName())
                                + " " + (s.getLastName() == null ? "" : s.getLastName()))
                        .classe(s.getClasse())
                        .enrollmentDate(s.getEnrollmentDate())
                        .statut(s.getStatus())
                        .build())
                .toList();

        return DashboardStatsDTO.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalClasses(totalClasses)
                .totalRevenue(totalRevenue)
                .totalPending(totalPending)
                .tauxRecouvrement(tauxRecouvrement)
                .tauxAbsence(tauxAbsence)
                .moyenneGenerale(moyenneGenerale)
                .studentsByNiveau(studentsByNiveau)
                .absencesToday(absencesToday)
                .newEnrollmentsThisMonth(newEnrollmentsThisMonth)
                .eventsThisMonth(eventsThisMonth)
                .weeklyAttendance(weeklyAttendance)
                .upcomingEvents(upcomingEvents)
                .recentStudents(recentStudents)
                .build();
    }

    private List<DayAttendanceDTO> buildWeeklyAttendance(LocalDate today, List<Absence> allAbsences, long totalStudents) {
        // Find the most recent Monday-Friday window (the current week if today is a weekday,
        // otherwise the previous week)
        LocalDate friday = today;
        if (today.getDayOfWeek() == DayOfWeek.SATURDAY) friday = today.minusDays(1);
        else if (today.getDayOfWeek() == DayOfWeek.SUNDAY) friday = today.minusDays(2);
        LocalDate monday = friday.minusDays(friday.getDayOfWeek().getValue() - 1);

        String[] labels = {"Lun", "Mar", "Mer", "Jeu", "Ven"};
        List<DayAttendanceDTO> out = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LocalDate d = monday.plusDays(i);
            long absents = allAbsences.stream()
                    .filter(a -> "ABSENCE".equalsIgnoreCase(a.getType()))
                    .filter(a -> d.equals(a.getDate()))
                    .count();
            long presents = Math.max(0L, totalStudents - absents);
            out.add(DayAttendanceDTO.builder().jour(labels[i]).presents(presents).absents(absents).build());
        }
        return out;
    }

    /**
     * Per-class aggregate stats for a given trimestre.
     * Returns one entry per class with average grade, success rate (% notes >= 10/20),
     * presence rate, and absence/retard counts.
     */
    public List<ClassStatsDTO> getClassStats(int trimestre) {
        // School-day baseline used for the presence rate when no `expected sessions`
        // field is tracked. Roughly: one trimestre ≈ 60 school days.
        final int trimestreSchoolDays = 60;

        List<Classe> classes = classeRepository.findAll();
        List<ClassStatsDTO> out = new ArrayList<>();

        for (Classe c : classes) {
            Niveau n = c.getNiveau();
            String fullName = buildClasseFullName(n.getName(), c.getLetter());
            List<com.schoolSys.schooolSys.student.Student> students =
                    studentRepository.findByClasse(fullName);
            long nbEleves = students.size();

            // ── Grades ─────────────────────────────────────────────
            List<Note> notes = noteRepository.findByExamenClasseIdAndTrimestre(c.getId(), trimestre);
            double moyenne = 0;
            double tauxReussite = 0;
            if (!notes.isEmpty()) {
                double sum = 0;
                int passed = 0;
                int counted = 0;
                for (Note note : notes) {
                    if (note.getValeur() == null) continue;
                    sum += note.getValeur();
                    if (note.getValeur() >= 10) passed++;
                    counted++;
                }
                if (counted > 0) {
                    moyenne = Math.round((sum / counted) * 10.0) / 10.0;
                    tauxReussite = Math.round(((double) passed / counted) * 1000.0) / 10.0;
                }
            }

            // ── Absences ───────────────────────────────────────────
            List<UUID> studentIds = students.stream()
                    .map(com.schoolSys.schooolSys.student.Student::getId)
                    .toList();
            long totalAbsences = 0;
            long totalRetards = 0;
            long totalJustifiees = 0;
            if (!studentIds.isEmpty()) {
                List<Absence> absences = absenceRepository.findByEleveIdIn(studentIds);
                for (Absence a : absences) {
                    String type = a.getType() == null ? "" : a.getType().toUpperCase();
                    if ("RETARD".equals(type)) {
                        totalRetards++;
                    } else {
                        totalAbsences++;
                        if (Boolean.TRUE.equals(a.getJustifie())) totalJustifiees++;
                    }
                }
            }

            double tauxPresence = 100.0;
            if (nbEleves > 0 && totalAbsences > 0) {
                double expected = (double) nbEleves * trimestreSchoolDays;
                double present = Math.max(0, expected - totalAbsences);
                tauxPresence = Math.round((present / expected) * 1000.0) / 10.0;
            }

            out.add(ClassStatsDTO.builder()
                    .classeId(c.getId())
                    .classeName(fullName)
                    .niveauName(n.getName())
                    .nbEleves(nbEleves)
                    .moyenne(moyenne)
                    .tauxReussite(tauxReussite)
                    .tauxPresence(tauxPresence)
                    .totalAbsences(totalAbsences)
                    .totalRetards(totalRetards)
                    .totalAbsencesJustifiees(totalJustifiees)
                    .build());
        }

        // Sort by fullName for stable display
        out.sort(Comparator.comparing(ClassStatsDTO::getClasseName));
        return out;
    }

    /**
     * MOB-FUNC-016 — distribution des notes par tranches pour une classe + trimestre,
     * optionnellement filtrée sur une matière.
     */
    public ClassDrillDownDTO.GradeDistribution getGradeDistribution(UUID classeId, int trimestre, UUID moduleId) {
        List<Note> notes = noteRepository.findByExamenClasseIdAndTrimestre(classeId, trimestre);
        if (moduleId != null) {
            notes = notes.stream()
                    .filter(n -> n.getExamen() != null && n.getExamen().getModule() != null)
                    .filter(n -> moduleId.equals(n.getExamen().getModule().getId()))
                    .collect(Collectors.toList());
        }

        int[] counts = new int[4]; // 0-5, 5-10, 10-15, 15-20
        int total = 0;
        for (Note n : notes) {
            if (n.getValeur() == null) continue;
            double v = n.getValeur().doubleValue();
            int idx = v < 5 ? 0 : v < 10 ? 1 : v < 15 ? 2 : 3;
            counts[idx]++;
            total++;
        }

        String[] labels = {"0-5", "5-10", "10-15", "15-20"};
        List<ClassDrillDownDTO.GradeBucket> buckets = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            buckets.add(ClassDrillDownDTO.GradeBucket.builder()
                    .range(labels[i])
                    .count(counts[i])
                    .build());
        }
        return ClassDrillDownDTO.GradeDistribution.builder()
                .buckets(buckets)
                .totalNotes(total)
                .build();
    }

    /**
     * MOB-FUNC-017 — évolution trimestrielle (T1, T2, T3) pour une classe,
     * avec la moyenne école comparée pour chaque trimestre.
     */
    public ClassDrillDownDTO.TrimestreEvolution getTrimestreEvolution(UUID classeId) {
        List<ClassDrillDownDTO.TrimestrePoint> points = new ArrayList<>();
        for (int t = 1; t <= 3; t++) {
            List<ClassStatsDTO> all = getClassStats(t);
            double moyenneEcole = all.stream()
                    .filter(c -> c.getMoyenne() > 0)
                    .mapToDouble(ClassStatsDTO::getMoyenne)
                    .average()
                    .orElse(0.0);
            ClassStatsDTO mine = all.stream()
                    .filter(c -> classeId.equals(c.getClasseId()))
                    .findFirst()
                    .orElse(null);
            points.add(ClassDrillDownDTO.TrimestrePoint.builder()
                    .trimestre(t)
                    .moyenne(mine != null ? mine.getMoyenne() : 0.0)
                    .tauxReussite(mine != null ? mine.getTauxReussite() : 0.0)
                    .tauxPresence(mine != null ? mine.getTauxPresence() : 0.0)
                    .moyenneEcole(Math.round(moyenneEcole * 10.0) / 10.0)
                    .build());
        }
        return ClassDrillDownDTO.TrimestreEvolution.builder().points(points).build();
    }

    /**
     * MOB-FUNC-018 — top {@code limit} et flop {@code limit} élèves d'une classe
     * pour un trimestre donné (calcul de moyenne sur les notes du trimestre).
     */
    public ClassDrillDownDTO.TopFlop getTopFlop(UUID classeId, int trimestre, int limit) {
        Classe c = classeRepository.findById(classeId).orElse(null);
        if (c == null) {
            return ClassDrillDownDTO.TopFlop.builder().top(List.of()).flop(List.of()).build();
        }
        String fullName = buildClasseFullName(c.getNiveau().getName(), c.getLetter());
        List<com.schoolSys.schooolSys.student.Student> students = studentRepository.findByClasse(fullName);

        List<ClassDrillDownDTO.StudentRank> ranks = students.stream()
                .map(s -> {
                    List<Note> notes = noteRepository.findByStudentIdAndTrimestre(s.getId(), trimestre);
                    double avg = notes.stream()
                            .filter(n -> n.getValeur() != null)
                            .mapToDouble(n -> n.getValeur().doubleValue())
                            .average()
                            .orElse(-1.0);
                    if (avg < 0) return null;
                    return ClassDrillDownDTO.StudentRank.builder()
                            .studentId(s.getId())
                            .prenom(s.getFirstName())
                            .nom(s.getLastName())
                            .moyenne(Math.round(avg * 10.0) / 10.0)
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparingDouble(ClassDrillDownDTO.StudentRank::getMoyenne).reversed())
                .collect(Collectors.toList());

        // assign rangs
        for (int i = 0; i < ranks.size(); i++) {
            ranks.get(i).setRang(i + 1);
        }

        List<ClassDrillDownDTO.StudentRank> top = ranks.stream().limit(limit).collect(Collectors.toList());
        List<ClassDrillDownDTO.StudentRank> flop = ranks.size() <= limit ? List.of() : ranks.stream()
                .skip(Math.max(0, ranks.size() - limit))
                .sorted(Comparator.comparingDouble(ClassDrillDownDTO.StudentRank::getMoyenne))
                .collect(Collectors.toList());

        return ClassDrillDownDTO.TopFlop.builder().top(top).flop(flop).build();
    }

    /**
     * MOB-FUNC-029 — liste complète des élèves d'une classe avec moyenne + absences,
     * triée par moyenne décroissante (rang assigné).
     */
    public List<ClassDrillDownDTO.StudentStats> getClassStudentsStats(UUID classeId, int trimestre) {
        Classe c = classeRepository.findById(classeId).orElse(null);
        if (c == null) return List.of();
        String fullName = buildClasseFullName(c.getNiveau().getName(), c.getLetter());
        List<com.schoolSys.schooolSys.student.Student> students = studentRepository.findByClasse(fullName);
        if (students.isEmpty()) return List.of();

        List<UUID> studentIds = students.stream().map(com.schoolSys.schooolSys.student.Student::getId).toList();
        List<Absence> absences = absenceRepository.findByEleveIdIn(studentIds);
        Map<UUID, long[]> absByStudent = new HashMap<>();
        for (Absence a : absences) {
            if (Boolean.TRUE.equals(a.getDeleted())) continue;
            long[] counts = absByStudent.computeIfAbsent(a.getEleveId(), k -> new long[2]);
            if ("RETARD".equals(a.getType())) counts[1]++;
            else counts[0]++;
        }

        List<ClassDrillDownDTO.StudentStats> list = students.stream()
                .map(s -> {
                    List<Note> notes = noteRepository.findByStudentIdAndTrimestre(s.getId(), trimestre);
                    double avg = notes.stream()
                            .filter(n -> n.getValeur() != null)
                            .mapToDouble(n -> n.getValeur().doubleValue())
                            .average()
                            .orElse(-1.0);
                    long[] absCounts = absByStudent.getOrDefault(s.getId(), new long[]{0, 0});
                    return ClassDrillDownDTO.StudentStats.builder()
                            .studentId(s.getId())
                            .prenom(s.getFirstName())
                            .nom(s.getLastName())
                            .matricule(s.getMatricule())
                            .moyenne(avg < 0 ? null : Math.round(avg * 10.0) / 10.0)
                            .totalAbsences((int) absCounts[0])
                            .totalRetards((int) absCounts[1])
                            .build();
                })
                .sorted((a, b) -> {
                    Double am = a.getMoyenne();
                    Double bm = b.getMoyenne();
                    if (am == null && bm == null) return 0;
                    if (am == null) return 1;
                    if (bm == null) return -1;
                    return Double.compare(bm, am);
                })
                .collect(Collectors.toList());

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getMoyenne() != null) list.get(i).setRang(i + 1);
        }
        return list;
    }

    private String buildClasseFullName(String niveauName, String letter) {
        StringBuilder digits = new StringBuilder();
        for (char ch : niveauName.toCharArray()) {
            if (Character.isDigit(ch)) digits.append(ch);
            else break;
        }
        return digits.toString() + letter;
    }

    public List<MonthlyTrendDTO> getMonthlyTrends(String anneeScolaire) {
        List<MonthlyTrendDTO> trends = new ArrayList<>();
        Locale fr = Locale.FRENCH;

        // Parse year range (e.g., "2025-2026")
        int startYear;
        int endYear;
        try {
            String[] parts = anneeScolaire.split("-");
            startYear = Integer.parseInt(parts[0]);
            endYear = parts.length > 1 ? Integer.parseInt(parts[1]) : startYear + 1;
        } catch (Exception e) {
            startYear = 2025;
            endYear = 2026;
        }

        // Academic year: September to June
        int[][] monthYears = {
                {9, startYear}, {10, startYear}, {11, startYear}, {12, startYear},
                {1, endYear}, {2, endYear}, {3, endYear}, {4, endYear},
                {5, endYear}, {6, endYear}
        };

        List<UUID> allStudentIds = studentRepository.findAll().stream()
                .map(Student::getId)
                .toList();

        for (int[] my : monthYears) {
            int month = my[0];
            int year = my[1];

            String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, fr);
            monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

            // Inscriptions count for this month
            long inscriptions = studentRepository.findAll().stream()
                    .filter(s -> s.getEnrollmentDate() != null
                            && s.getEnrollmentDate().getMonthValue() == month
                            && s.getEnrollmentDate().getYear() == year)
                    .count();

            // Payment total for this month
            String moisKey = String.format("%02d/%d", month, year);
            BigDecimal paiements = paiementRepository.sumMontantPayeByMoisAndAnneeScolaire(moisKey, anneeScolaire);

            // Absences count for this month
            long absences = 0;
            if (!allStudentIds.isEmpty()) {
                try {
                    absences = absenceRepository.findByEleveIdInAndMonthAndYear(allStudentIds, month, year).size();
                } catch (Exception ignored) {
                    // If the query fails due to empty list, default to 0
                }
            }

            trends.add(MonthlyTrendDTO.builder()
                    .month(monthName)
                    .inscriptions(inscriptions)
                    .paiements(paiements)
                    .absences(absences)
                    .build());
        }

        return trends;
    }
}
