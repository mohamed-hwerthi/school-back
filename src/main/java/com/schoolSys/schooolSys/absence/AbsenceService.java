package com.schoolSys.schooolSys.absence;

import java.util.UUID;

import com.schoolSys.schooolSys.absence.dto.*;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.niveau.Niveau;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final JustificatifRepository justificatifRepository;
    private final StudentRepository studentRepository;
    private final AbsenceSettingsRepository settingsRepository;
    private final AbsenceNotificationService notificationService;
    private final ClasseRepository classeRepository;
    private final AnneeScolaireProvider anneeScolaireProvider;
    private final CurrentUserContext currentUser;

    /** Silent filter for list endpoints. */
    private boolean isInScope(UUID studentId) {
        try {
            currentUser.assertCanAccessStudent(studentId);
            return true;
        } catch (AccessDeniedException e) {
            return false;
        }
    }

    @Transactional
    public List<AbsenceResponseDTO> batchCreate(AbsenceBatchRequestDTO request) {
        // Block writes on students outside the current user's scope.
        request.getAbsences().forEach(dto ->
                currentUser.assertCanAccessStudent(dto.getEleveId()));
        String currentAnnee = anneeScolaireProvider.getCurrentAnneeScolaire();
        List<Absence> absences = request.getAbsences().stream()
            .map(dto -> Absence.builder()
                .eleveId(dto.getEleveId())
                .date(dto.getDate())
                .type(dto.getType())
                .anneeScolaire(currentAnnee)
                .seance(dto.getSeance())
                .heureArrivee(dto.getHeureArrivee())
                .justifie(dto.getJustifie() != null ? dto.getJustifie() : false)
                .motif(dto.getMotif())
                .enseignantId(dto.getEnseignantId())
                .build())
            .collect(Collectors.toList());
        List<Absence> saved = absenceRepository.saveAll(absences);

        // ABS-005: Notify parents for unjustified absences
        saved.stream()
            .filter(a -> !Boolean.TRUE.equals(a.getJustifie()))
            .forEach(notificationService::notifyParentAbsence);

        return saved.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AbsenceResponseDTO> getByClasseAndDate(UUID classeId, LocalDate date, String type) {
        // A teacher may only read absences of a class he is affected to. When no
        // classeId is given ("toutes les classes"), the result is row-level scoped
        // to his students instead of being refused outright.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)
                && classeId != null
                && !currentUser.teacherTeachesClasse(classeId)) {
            throw new AccessDeniedException("Vous n'enseignez pas dans cette classe.");
        }
        List<Absence> absences;
        if (classeId == null) {
            absences = absenceRepository.findAll().stream()
                .filter(a -> date.equals(a.getDate()))
                .filter(a -> type == null || type.isBlank() || type.equals(a.getType()))
                .filter(a -> isInScope(a.getEleveId()))
                .collect(Collectors.toList());
        } else {
            List<UUID> eleveIds = getEleveIdsByClasse(classeId);
            if (eleveIds.isEmpty()) return List.of();
            if (type != null && !type.isBlank()) {
                absences = absenceRepository.findByDateAndEleveIdInAndType(date, eleveIds, type);
            } else {
                absences = absenceRepository.findByDateAndEleveIdIn(date, eleveIds);
            }
        }
        return absences.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeuilleJourDTO> getFeuillesByDate(LocalDate date) {
        List<Classe> classes = classeRepository.findAll();
        // A teacher only sees the daily sheets of his own classes.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            Set<UUID> scoped = currentUser.getScopedClasseIdsForTeacher();
            classes = classes.stream()
                .filter(c -> scoped.contains(c.getId()))
                .collect(Collectors.toList());
        }
        return classes.stream()
            .map(c -> {
                String fullName = resolveClasseFullName(c.getId());
                List<Student> eleves = fullName == null ? List.of() :
                    studentRepository.findAll().stream()
                        .filter(s -> fullName.equals(s.getClasse()))
                        .collect(Collectors.toList());
                List<UUID> eleveIds = eleves.stream().map(Student::getId).collect(Collectors.toList());
                List<Absence> absences = eleveIds.isEmpty()
                    ? List.of()
                    : absenceRepository.findByDateAndEleveIdIn(date, eleveIds);
                int abs = (int) absences.stream().filter(a -> "ABSENCE".equals(a.getType())).count();
                int ret = (int) absences.stream().filter(a -> "RETARD".equals(a.getType())).count();
                int just = (int) absences.stream().filter(Absence::getJustifie).count();
                return FeuilleJourDTO.builder()
                    .classeId(c.getId())
                    .classeLabel(fullName)
                    .niveauName(c.getNiveau() != null ? c.getNiveau().getName() : null)
                    .date(date)
                    .totalEleves(eleves.size())
                    .absences(abs)
                    .retards(ret)
                    .justifiees(just)
                    .build();
            })
            .filter(f -> f.getTotalEleves() > 0 || f.getAbsences() > 0 || f.getRetards() > 0)
            .sorted(Comparator.comparing(FeuilleJourDTO::getClasseLabel, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());
    }

    public List<AbsenceResponseDTO> getByEleve(UUID eleveId) {
        currentUser.assertCanAccessStudent(eleveId);
        return absenceRepository.findByEleveId(eleveId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public AbsenceStatsDTO getStats(UUID classeId, int mois, int annee) {
        if (currentUser.hasRole(UserRole.ENSEIGNANT) && !currentUser.teacherTeachesClasse(classeId)) {
            throw new AccessDeniedException("Vous n'enseignez pas dans cette classe.");
        }
        List<UUID> eleveIds = getEleveIdsByClasse(classeId);
        if (eleveIds.isEmpty()) {
            return AbsenceStatsDTO.builder()
                .classeId(classeId).mois(mois).annee(annee)
                .totalAbsences(0).totalRetards(0)
                .totalJustifiees(0).totalNonJustifiees(0)
                .tauxAbsenteisme(0.0)
                .alertLevel("NORMAL")
                .elevesEnAlerte(List.of())
                .build();
        }

        AbsenceSettings settings = getSettings();
        List<Absence> absences = absenceRepository.findByEleveIdInAndMonthAndYear(eleveIds, mois, annee);
        long totalAbsences = absences.stream().filter(a -> "ABSENCE".equals(a.getType())).count();
        long totalRetards = absences.stream().filter(a -> "RETARD".equals(a.getType())).count();
        long totalJustifiees = absences.stream().filter(Absence::getJustifie).count();
        long totalNonJustifiees = absences.size() - totalJustifiees;
        double taux = eleveIds.size() > 0 ? (double) totalAbsences / eleveIds.size() * 100 : 0;

        // Calculate per-student alerts
        Map<UUID, Long> absencesByEleve = absences.stream()
            .filter(a -> "ABSENCE".equals(a.getType()))
            .collect(Collectors.groupingBy(Absence::getEleveId, Collectors.counting()));

        List<AbsenceStatsDTO.EleveAlertDTO> elevesEnAlerte = new ArrayList<>();
        String overallAlert = "NORMAL";

        for (Map.Entry<UUID, Long> entry : absencesByEleve.entrySet()) {
            long count = entry.getValue();
            String level = computeAlertLevel(count, settings);
            if (!"NORMAL".equals(level)) {
                Student student = studentRepository.findById(entry.getKey()).orElse(null);
                String nom = student != null ? student.getLastName() : "";
                String prenom = student != null ? student.getFirstName() : "";
                elevesEnAlerte.add(AbsenceStatsDTO.EleveAlertDTO.builder()
                    .eleveId(entry.getKey())
                    .nom(nom)
                    .prenom(prenom)
                    .absences(count)
                    .alertLevel(level)
                    .build());
            }
            if ("ROUGE".equals(level)) {
                overallAlert = "ROUGE";
            } else if ("JAUNE".equals(level) && !"ROUGE".equals(overallAlert)) {
                overallAlert = "JAUNE";
            }
        }

        // Sort alerts: ROUGE first, then JAUNE
        elevesEnAlerte.sort((a, b) -> {
            if (a.getAlertLevel().equals(b.getAlertLevel())) return 0;
            return "ROUGE".equals(a.getAlertLevel()) ? -1 : 1;
        });

        return AbsenceStatsDTO.builder()
            .classeId(classeId).mois(mois).annee(annee)
            .totalAbsences(totalAbsences).totalRetards(totalRetards)
            .totalJustifiees(totalJustifiees).totalNonJustifiees(totalNonJustifiees)
            .tauxAbsenteisme(Math.round(taux * 100.0) / 100.0)
            .alertLevel(overallAlert)
            .elevesEnAlerte(elevesEnAlerte)
            .build();
    }

    @Transactional
    public AbsenceResponseDTO justifier(UUID absenceId) {
        Absence absence = absenceRepository.findById(absenceId)
            .orElseThrow(() -> new ResourceNotFoundException("Absence", absenceId));
        currentUser.assertCanAccessStudent(absence.getEleveId());
        absence.setJustifie(true);
        return toDto(absenceRepository.save(absence));
    }

    /**
     * ABS-008: Submit justification with motif and optional file.
     */
    @Transactional
    public AbsenceResponseDTO submitJustification(UUID absenceId, JustificationRequestDTO request) {
        Absence absence = absenceRepository.findById(absenceId)
            .orElseThrow(() -> new ResourceNotFoundException("Absence", absenceId));
        currentUser.assertCanAccessStudent(absence.getEleveId());
        absence.setJustifie(true);
        absence.setMotif(request.getMotif());
        Absence saved = absenceRepository.save(absence);

        // Create a justificatif record
        Justificatif justificatif = Justificatif.builder()
            .absence(saved)
            .fichierUrl(request.getFichierUrl())
            .dateSoumission(LocalDateTime.now())
            .valide(null) // pending validation
            .build();
        justificatifRepository.save(justificatif);

        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Absence absence = absenceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Absence", id));
        currentUser.assertCanAccessStudent(absence.getEleveId());
        absenceRepository.deleteById(id);
    }

    // ---- ABS-006: Settings ----

    public AbsenceSettingsDTO getAbsenceSettings() {
        AbsenceSettings settings = getSettings();
        return toSettingsDto(settings);
    }

    @Transactional
    public AbsenceSettingsDTO updateAbsenceSettings(AbsenceSettingsDTO dto) {
        AbsenceSettings settings = getSettings();
        if (dto.getSeuilAlerteJaune() != null) settings.setSeuilAlerteJaune(dto.getSeuilAlerteJaune());
        if (dto.getSeuilAlerteRouge() != null) settings.setSeuilAlerteRouge(dto.getSeuilAlerteRouge());
        if (dto.getNotificationAuto() != null) settings.setNotificationAuto(dto.getNotificationAuto());
        if (dto.getNotificationEmail() != null) settings.setNotificationEmail(dto.getNotificationEmail());
        if (dto.getNotificationSms() != null) settings.setNotificationSms(dto.getNotificationSms());
        return toSettingsDto(settingsRepository.save(settings));
    }

    // ---- ABS-007: Rapport mensuel ----

    public RapportPresenceDTO getRapportMensuel(UUID classeId, int mois, int annee) {
        if (currentUser.hasRole(UserRole.ENSEIGNANT) && !currentUser.teacherTeachesClasse(classeId)) {
            throw new AccessDeniedException("Vous n'enseignez pas dans cette classe.");
        }
        List<Student> students = getStudentsByClasse(classeId);
        if (students.isEmpty()) {
            return RapportPresenceDTO.builder()
                .classeLabel("Classe " + classeId)
                .mois(mois).annee(annee)
                .totalEleves(0).totalAbsences(0).totalRetards(0)
                .tauxPresenceGlobal(100.0)
                .eleves(List.of())
                .build();
        }

        List<UUID> eleveIds = students.stream().map(Student::getId).collect(Collectors.toList());
        List<Absence> absences = absenceRepository.findByEleveIdInAndMonthAndYear(eleveIds, mois, annee);

        // Group by student
        Map<UUID, List<Absence>> byEleve = absences.stream()
            .collect(Collectors.groupingBy(Absence::getEleveId));

        // Approximate working days in a month
        int joursOuvrables = 22;

        List<RapportPresenceDTO.ElevePresenceDTO> eleveDtos = students.stream().map(s -> {
            List<Absence> sAbsences = byEleve.getOrDefault(s.getId(), List.of());
            long abs = sAbsences.stream().filter(a -> "ABSENCE".equals(a.getType())).count();
            long ret = sAbsences.stream().filter(a -> "RETARD".equals(a.getType())).count();
            long just = sAbsences.stream().filter(Absence::getJustifie).count();
            double tauxP = joursOuvrables > 0 ? Math.max(0, (1.0 - (double) abs / joursOuvrables) * 100) : 100.0;
            return RapportPresenceDTO.ElevePresenceDTO.builder()
                .eleveId(s.getId())
                .nom(s.getLastName())
                .prenom(s.getFirstName())
                .totalAbsences(abs)
                .totalRetards(ret)
                .totalJustifiees(just)
                .tauxPresence(Math.round(tauxP * 100.0) / 100.0)
                .build();
        }).collect(Collectors.toList());

        long totalAbs = absences.stream().filter(a -> "ABSENCE".equals(a.getType())).count();
        long totalRet = absences.stream().filter(a -> "RETARD".equals(a.getType())).count();
        double tauxGlobal = students.size() > 0 && joursOuvrables > 0
            ? Math.max(0, (1.0 - (double) totalAbs / (students.size() * joursOuvrables)) * 100) : 100.0;

        return RapportPresenceDTO.builder()
            .classeLabel("Classe " + classeId)
            .mois(mois).annee(annee)
            .totalEleves(students.size())
            .totalAbsences(totalAbs)
            .totalRetards(totalRet)
            .tauxPresenceGlobal(Math.round(tauxGlobal * 100.0) / 100.0)
            .eleves(eleveDtos)
            .build();
    }

    // ---- ABS-009: Historique presence par eleve ----

    public HistoriquePresenceDTO getHistorique(UUID eleveId) {
        currentUser.assertCanAccessStudent(eleveId);
        Student student = studentRepository.findById(eleveId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", eleveId));

        List<Absence> allAbsences = absenceRepository.findByEleveIdOrderByDateDesc(eleveId);
        AbsenceSettings settings = getSettings();

        long totalAbsences = allAbsences.stream().filter(a -> "ABSENCE".equals(a.getType())).count();
        long totalRetards = allAbsences.stream().filter(a -> "RETARD".equals(a.getType())).count();
        long totalJustifiees = allAbsences.stream().filter(Absence::getJustifie).count();

        // Approximate attendance rate (assuming ~200 school days/year is a rough default)
        double tauxPresence = 200 > 0 ? Math.max(0, (1.0 - (double) totalAbsences / 200) * 100) : 100.0;

        String alertLevel = computeAlertLevel(totalAbsences, settings);

        // Monthly breakdown
        Map<String, List<Absence>> byMonth = allAbsences.stream()
            .collect(Collectors.groupingBy(a -> a.getDate().getYear() + "-" + a.getDate().getMonthValue()));

        List<HistoriquePresenceDTO.MonthlyBreakdown> monthlyBreakdown = byMonth.entrySet().stream()
            .map(entry -> {
                String[] parts = entry.getKey().split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                List<Absence> monthAbsences = entry.getValue();
                long mAbs = monthAbsences.stream().filter(a -> "ABSENCE".equals(a.getType())).count();
                long mRet = monthAbsences.stream().filter(a -> "RETARD".equals(a.getType())).count();
                long mJust = monthAbsences.stream().filter(Absence::getJustifie).count();
                double mTaux = Math.max(0, (1.0 - (double) mAbs / 22) * 100);
                return HistoriquePresenceDTO.MonthlyBreakdown.builder()
                    .month(month).year(year)
                    .absences(mAbs).retards(mRet).justifiees(mJust)
                    .tauxPresence(Math.round(mTaux * 100.0) / 100.0)
                    .build();
            })
            .sorted((a, b) -> {
                int cmp = Integer.compare(b.getYear(), a.getYear());
                return cmp != 0 ? cmp : Integer.compare(b.getMonth(), a.getMonth());
            })
            .collect(Collectors.toList());

        // Recent absences (last 20)
        List<HistoriquePresenceDTO.RecentAbsence> recentAbsences = allAbsences.stream()
            .limit(20)
            .map(a -> HistoriquePresenceDTO.RecentAbsence.builder()
                .id(a.getId())
                .date(a.getDate())
                .type(a.getType())
                .seance(a.getSeance())
                .heureArrivee(a.getHeureArrivee())
                .justifie(a.getJustifie())
                .motif(a.getMotif())
                .build())
            .collect(Collectors.toList());

        return HistoriquePresenceDTO.builder()
            .eleveId(eleveId)
            .studentName(student.getFirstName() + " " + student.getLastName())
            .totalAbsences(totalAbsences)
            .totalRetards(totalRetards)
            .totalJustifiees(totalJustifiees)
            .tauxPresence(Math.round(tauxPresence * 100.0) / 100.0)
            .alertLevel(alertLevel)
            .monthlyBreakdown(monthlyBreakdown)
            .recentAbsences(recentAbsences)
            .build();
    }

    // ---- Private helpers ----

    private AbsenceSettings getSettings() {
        return settingsRepository.findAll().stream()
            .findFirst()
            .orElseGet(() -> settingsRepository.save(AbsenceSettings.builder().build()));
    }

    private String computeAlertLevel(long absences, AbsenceSettings settings) {
        if (absences >= settings.getSeuilAlerteRouge()) return "ROUGE";
        if (absences >= settings.getSeuilAlerteJaune()) return "JAUNE";
        return "NORMAL";
    }

    private String resolveClasseFullName(UUID classeId) {
        Classe classe = classeRepository.findById(classeId).orElse(null);
        if (classe == null) return null;
        Niveau n = classe.getNiveau();
        StringBuilder digits = new StringBuilder();
        for (char ch : n.getName().toCharArray()) {
            if (Character.isDigit(ch)) digits.append(ch);
            else break;
        }
        return digits + classe.getLetter();
    }

    @Transactional(readOnly = true)
    private List<UUID> getEleveIdsByClasse(UUID classeId) {
        String fullName = resolveClasseFullName(classeId);
        if (fullName == null) return List.of();
        return studentRepository.findAll().stream()
            .filter(s -> fullName.equals(s.getClasse()))
            .map(Student::getId)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private List<Student> getStudentsByClasse(UUID classeId) {
        String fullName = resolveClasseFullName(classeId);
        if (fullName == null) return List.of();
        return studentRepository.findAll().stream()
            .filter(s -> fullName.equals(s.getClasse()))
            .collect(Collectors.toList());
    }

    private AbsenceSettingsDTO toSettingsDto(AbsenceSettings s) {
        return AbsenceSettingsDTO.builder()
            .id(s.getId())
            .seuilAlerteJaune(s.getSeuilAlerteJaune())
            .seuilAlerteRouge(s.getSeuilAlerteRouge())
            .notificationAuto(s.getNotificationAuto())
            .notificationEmail(s.getNotificationEmail())
            .notificationSms(s.getNotificationSms())
            .build();
    }

    private AbsenceResponseDTO toDto(Absence a) {
        return AbsenceResponseDTO.builder()
            .id(a.getId())
            .eleveId(a.getEleveId())
            .date(a.getDate())
            .type(a.getType())
            .seance(a.getSeance())
            .heureArrivee(a.getHeureArrivee())
            .justifie(a.getJustifie())
            .motif(a.getMotif())
            .enseignantId(a.getEnseignantId())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }
}
