package com.schoolSys.schooolSys.parent;

import java.time.LocalDate;
import java.util.UUID;

import com.schoolSys.schooolSys.absence.Absence;
import com.schoolSys.schooolSys.absence.AbsenceRepository;
import com.schoolSys.schooolSys.absence.dto.AbsenceResponseDTO;
import com.schoolSys.schooolSys.bulletin.BulletinService;
import com.schoolSys.schooolSys.bulletin.dto.BulletinDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.devoir.Devoir;
import com.schoolSys.schooolSys.devoir.DevoirRepository;
import com.schoolSys.schooolSys.discipline.IncidentRepository;
import com.schoolSys.schooolSys.emploidutemps.EmploiDuTemps;
import com.schoolSys.schooolSys.emploidutemps.EmploiDuTempsRepository;
import com.schoolSys.schooolSys.emploidutemps.dto.EmploiDuTempsResponseDTO;
import com.schoolSys.schooolSys.examen.Examen;
import com.schoolSys.schooolSys.examen.ExamenRepository;
import com.schoolSys.schooolSys.finance.Paiement;
import com.schoolSys.schooolSys.finance.PaiementRepository;
import com.schoolSys.schooolSys.module.Module;
import com.schoolSys.schooolSys.module.ModuleRepository;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.note.dto.NoteResponseDTO;
import com.schoolSys.schooolSys.evenement.EvenementCalendrier;
import com.schoolSys.schooolSys.evenement.EvenementCalendrierRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.parent.dto.AlertsDTO;
import com.schoolSys.schooolSys.parent.dto.ChildCalendarDTO;
import com.schoolSys.schooolSys.parent.dto.ChildDTO;
import com.schoolSys.schooolSys.parent.dto.ChildProfileDTO;
import com.schoolSys.schooolSys.parent.dto.ChildProgressDTO;
import com.schoolSys.schooolSys.parent.dto.TrendDTO;
import com.schoolSys.schooolSys.parent.dto.UpcomingDTO;
import com.schoolSys.schooolSys.student.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentPortalService {

    private final ParentStudentRepository parentStudentRepository;
    private final NoteRepository noteRepository;
    private final AbsenceRepository absenceRepository;
    private final BulletinService bulletinService;
    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final DevoirRepository devoirRepository;
    private final ExamenRepository examenRepository;
    private final PaiementRepository paiementRepository;
    private final IncidentRepository incidentRepository;
    private final ModuleRepository moduleRepository;
    private final EvenementCalendrierRepository evenementRepository;
    private final ClasseRepository classeRepository;
    private final com.schoolSys.schooolSys.student.StudentRepository studentRepository;

    public List<ChildDTO> getChildren(UUID parentUserId) {
        return parentStudentRepository.findByParentUserId(parentUserId)
                .stream()
                .map(ps -> {
                    Student s = ps.getStudent();
                    return ChildDTO.builder()
                            .id(s.getId())
                            .firstName(s.getFirstName())
                            .lastName(s.getLastName())
                            .classe(s.getClasse())
                            .niveau(s.getNiveau())
                            .matricule(s.getMatricule())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<NoteResponseDTO> getChildNotes(UUID parentUserId, UUID studentId, Integer trimestre) {
        verifyParentLink(parentUserId, studentId);
        List<Note> notes = noteRepository.findByStudentIdAndTrimestre(studentId, trimestre);
        return notes.stream()
                .map(n -> NoteResponseDTO.builder()
                        .id(n.getId())
                        .studentId(n.getStudent().getId())
                        .studentName(n.getStudent().getFirstName() + " " + n.getStudent().getLastName())
                        .examenId(n.getExamen().getId())
                        .examenName(n.getExamen().getName())
                        .trimestre(n.getTrimestre())
                        .valeur(n.getValeur())
                        .observation(n.getObservation())
                        .build())
                .collect(Collectors.toList());
    }

    public List<AbsenceResponseDTO> getChildAbsences(UUID parentUserId, UUID studentId) {
        verifyParentLink(parentUserId, studentId);
        return absenceRepository.findByEleveId(studentId)
                .stream()
                .map(this::toAbsenceDto)
                .collect(Collectors.toList());
    }

    public BulletinDTO getChildBulletin(UUID parentUserId, UUID studentId, UUID classeId, Integer trimestre) {
        verifyParentLink(parentUserId, studentId);
        return bulletinService.getBulletin(classeId, studentId, trimestre, "etatique");
    }

    public List<EmploiDuTempsResponseDTO> getChildEmploiDuTemps(UUID parentUserId, UUID studentId) {
        verifyParentLink(parentUserId, studentId);

        // Get student's classe to find their schedule
        ParentStudent ps = parentStudentRepository.findByParentUserId(parentUserId)
                .stream()
                .filter(p -> p.getStudent().getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Parent-Student link not found"));

        String classeStr = ps.getStudent().getClasse();
        if (classeStr == null) return List.of();

        try {
            UUID classeId = UUID.fromString(classeStr);
            return emploiDuTempsRepository.findByClasseId(classeId)
                    .stream()
                    .map(this::toEmploiDto)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return List.of();
        }
    }

    /**
     * Devoirs à rendre, examens et paiements dus dans les {@code days} prochains jours.
     */
    public UpcomingDTO getChildUpcoming(UUID parentUserId, UUID studentId, int days) {
        verifyParentLink(parentUserId, studentId);
        Student student = parentStudentRepository.findByParentUserId(parentUserId)
                .stream()
                .map(ParentStudent::getStudent)
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(days);
        UUID classeId = parseClasseId(student.getClasse());

        List<UpcomingDTO.UpcomingDevoir> devoirs = classeId == null ? List.of()
                : devoirRepository.findByClasseIdOrderByDateLimiteDesc(classeId).stream()
                        .filter(d -> Boolean.FALSE.equals(d.getDeleted()))
                        .filter(d -> "PUBLIE".equals(d.getStatut()))
                        .filter(d -> d.getDateLimite() != null
                                && !d.getDateLimite().isBefore(today)
                                && !d.getDateLimite().isAfter(horizon))
                        .sorted(Comparator.comparing(Devoir::getDateLimite))
                        .map(this::toUpcomingDevoir)
                        .collect(Collectors.toList());

        List<UpcomingDTO.UpcomingExamen> examens = classeId == null ? List.of()
                : examenRepository.findFiltered(null, classeId, null, null).stream()
                        .filter(e -> Boolean.FALSE.equals(e.getDeleted()))
                        .filter(e -> e.getDateLimiteSaisie() != null
                                && !e.getDateLimiteSaisie().isBefore(today)
                                && !e.getDateLimiteSaisie().isAfter(horizon))
                        .sorted(Comparator.comparing(Examen::getDateLimiteSaisie))
                        .map(this::toUpcomingExamen)
                        .collect(Collectors.toList());

        List<UpcomingDTO.UpcomingPaiement> paiements = paiementRepository.findByStudentId(studentId).stream()
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                .filter(p -> p.getStatut() == Paiement.StatutPaiement.EN_ATTENTE
                        || p.getStatut() == Paiement.StatutPaiement.PARTIEL
                        || p.getStatut() == Paiement.StatutPaiement.EN_RETARD)
                .sorted(Comparator.comparing(Paiement::getCreatedAt))
                .map(this::toUpcomingPaiement)
                .collect(Collectors.toList());

        return UpcomingDTO.builder()
                .devoirs(devoirs)
                .examens(examens)
                .paiements(paiements)
                .build();
    }

    /**
     * Moyenne par trimestre pour la sparkline (3 points T1 → T3).
     */
    public TrendDTO getChildTrend(UUID parentUserId, UUID studentId) {
        verifyParentLink(parentUserId, studentId);
        List<TrendDTO.TrendPoint> points = List.of(1, 2, 3).stream()
                .map(t -> {
                    List<Note> notes = noteRepository.findByStudentIdAndTrimestre(studentId, t);
                    double avg = notes.stream()
                            .filter(n -> n.getValeur() != null)
                            .mapToDouble(n -> n.getValeur().doubleValue())
                            .average()
                            .orElse(0.0);
                    return TrendDTO.TrendPoint.builder()
                            .trimestre(t)
                            .moyenne(Math.round(avg * 10.0) / 10.0)
                            .noteCount(notes.size())
                            .build();
                })
                .collect(Collectors.toList());
        return TrendDTO.builder().points(points).build();
    }

    /**
     * Compteurs d'alertes (absences non justifiées 7j, retards 30j, incidents 30j).
     */
    public AlertsDTO getChildAlerts(UUID parentUserId, UUID studentId) {
        verifyParentLink(parentUserId, studentId);
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate monthAgo = today.minusDays(30);

        List<Absence> absences = absenceRepository.findByEleveId(studentId);

        int absencesNonJustifiees = (int) absences.stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .filter(a -> "ABSENCE".equals(a.getType()))
                .filter(a -> Boolean.FALSE.equals(a.getJustifie()))
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(weekAgo))
                .count();

        int retards = (int) absences.stream()
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .filter(a -> "RETARD".equals(a.getType()))
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(monthAgo))
                .count();

        int incidentsRecents = (int) incidentRepository.findByEleveId(studentId).stream()
                .filter(i -> Boolean.FALSE.equals(i.getDeleted()))
                .filter(i -> i.getDate() != null && !i.getDate().isBefore(monthAgo))
                .count();

        return AlertsDTO.builder()
                .absencesNonJustifiees(absencesNonJustifiees)
                .retards(retards)
                .incidentsRecents(incidentsRecents)
                .build();
    }

    /**
     * MOB-FUNC-007 — progression de l'enfant par matière sur les 3 trimestres.
     */
    public ChildProgressDTO getChildProgress(UUID parentUserId, UUID studentId) {
        verifyParentLink(parentUserId, studentId);

        // 3 sets de notes par trimestre
        List<Note> t1 = noteRepository.findByStudentIdAndTrimestre(studentId, 1);
        List<Note> t2 = noteRepository.findByStudentIdAndTrimestre(studentId, 2);
        List<Note> t3 = noteRepository.findByStudentIdAndTrimestre(studentId, 3);

        // Indexer par module
        java.util.Set<UUID> moduleIds = new java.util.HashSet<>();
        java.util.stream.Stream.of(t1, t2, t3).flatMap(List::stream)
                .filter(n -> n.getExamen() != null && n.getExamen().getModule() != null)
                .forEach(n -> moduleIds.add(n.getExamen().getModule().getId()));

        List<ChildProgressDTO.MatiereProgress> matieres = moduleIds.stream()
                .map(mid -> {
                    Module mod = moduleRepository.findById(mid).orElse(null);
                    Double m1 = avgForModule(t1, mid);
                    Double m2 = avgForModule(t2, mid);
                    Double m3 = avgForModule(t3, mid);
                    String tendance = computeTendance(m1, m2, m3);
                    return ChildProgressDTO.MatiereProgress.builder()
                            .moduleId(mid)
                            .moduleNom(mod != null ? mod.getName() : "?")
                            .t1(m1)
                            .t2(m2)
                            .t3(m3)
                            .tendance(tendance)
                            .build();
                })
                .sorted(Comparator.comparing(ChildProgressDTO.MatiereProgress::getModuleNom))
                .collect(Collectors.toList());

        return ChildProgressDTO.builder().matieres(matieres).build();
    }

    /**
     * MOB-FUNC-008 — événements (devoirs, examens, événements école) de l'enfant
     * entre deux dates incluses.
     */
    public ChildCalendarDTO getChildCalendar(UUID parentUserId, UUID studentId, LocalDate from, LocalDate to) {
        verifyParentLink(parentUserId, studentId);
        Student student = findChildOrThrow(parentUserId, studentId);
        UUID classeId = parseClasseId(student.getClasse());

        List<ChildCalendarDTO.CalendarEvent> events = new java.util.ArrayList<>();

        // Devoirs
        if (classeId != null) {
            devoirRepository.findByClasseIdOrderByDateLimiteDesc(classeId).stream()
                    .filter(d -> Boolean.FALSE.equals(d.getDeleted()))
                    .filter(d -> d.getDateLimite() != null
                            && !d.getDateLimite().isBefore(from)
                            && !d.getDateLimite().isAfter(to))
                    .forEach(d -> {
                        String modName = moduleRepository.findById(d.getModuleId())
                                .map(Module::getName).orElse(null);
                        events.add(ChildCalendarDTO.CalendarEvent.builder()
                                .id(d.getId())
                                .type("DEVOIR")
                                .titre(d.getTitre())
                                .subtitle(modName)
                                .date(d.getDateLimite())
                                .couleur("#3b82f6")
                                .build());
                    });

            examenRepository.findFiltered(null, classeId, null, null).stream()
                    .filter(e -> Boolean.FALSE.equals(e.getDeleted()))
                    .filter(e -> e.getDateLimiteSaisie() != null
                            && !e.getDateLimiteSaisie().isBefore(from)
                            && !e.getDateLimiteSaisie().isAfter(to))
                    .forEach(e -> events.add(ChildCalendarDTO.CalendarEvent.builder()
                            .id(e.getId())
                            .type("EXAMEN")
                            .titre(e.getName())
                            .subtitle(e.getModule() != null ? e.getModule().getName() : null)
                            .date(e.getDateLimiteSaisie())
                            .couleur("#f59e0b")
                            .build()));
        }

        // Événements école (pas de soft-delete sur EvenementCalendrier)
        evenementRepository.findByDateDebutBetweenOrderByDateDebutAsc(from, to)
                .forEach(e -> events.add(ChildCalendarDTO.CalendarEvent.builder()
                        .id(e.getId())
                        .type("EVENEMENT")
                        .titre(e.getTitre())
                        .subtitle(e.getLieu())
                        .date(e.getDateDebut())
                        .couleur(e.getCouleur() != null ? e.getCouleur() : "#10b981")
                        .build()));

        events.sort(Comparator.comparing(ChildCalendarDTO.CalendarEvent::getDate));
        return ChildCalendarDTO.builder().events(events).build();
    }

    /**
     * MOB-FUNC-010 — profil complet de l'enfant avec bulletin courant + classement.
     */
    public ChildProfileDTO getChildFullProfile(UUID parentUserId, UUID studentId, int trimestre) {
        verifyParentLink(parentUserId, studentId);
        Student student = findChildOrThrow(parentUserId, studentId);
        UUID classeId = parseClasseId(student.getClasse());

        // Bulletin courant (best-effort — peut échouer si pas de notes)
        BulletinDTO bulletin = null;
        if (classeId != null) {
            try {
                bulletin = bulletinService.getBulletin(classeId, studentId, trimestre, "etatique");
            } catch (Exception ignored) {
                // pas de bulletin disponible — on ignore
            }
        }

        // Classement + effectif
        Integer rang = null;
        Integer effectif = null;
        Double myAvg = null;
        if (classeId != null) {
            Classe c = classeRepository.findById(classeId).orElse(null);
            if (c != null) {
                String fullName = c.getNiveau().getName() + " " + c.getLetter();
                // students dans cette classe via student.classe string ; fallback fullName=getClasse()
                List<Student> classmates = studentRepository.findByClasse(student.getClasse());
                effectif = classmates.size();

                List<double[]> avgs = classmates.stream()
                        .map(s -> {
                            double a = noteRepository.findByStudentIdAndTrimestre(s.getId(), trimestre).stream()
                                    .filter(n -> n.getValeur() != null)
                                    .mapToDouble(n -> n.getValeur().doubleValue())
                                    .average()
                                    .orElse(-1.0);
                            return new double[]{a, s.getId().equals(studentId) ? 1 : 0};
                        })
                        .filter(arr -> arr[0] >= 0)
                        .sorted((a, b) -> Double.compare(b[0], a[0]))
                        .collect(Collectors.toList());

                for (int i = 0; i < avgs.size(); i++) {
                    if (avgs.get(i)[1] == 1) {
                        rang = i + 1;
                        myAvg = Math.round(avgs.get(i)[0] * 10.0) / 10.0;
                        break;
                    }
                }
            }
        }

        return ChildProfileDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .matricule(student.getMatricule())
                .niveau(student.getNiveau())
                .classe(student.getClasse())
                .sexe(student.getSex())
                .dateOfBirth(student.getDateOfBirth())
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus())
                .currentBulletin(bulletin)
                .rangClasse(rang)
                .effectifClasse(effectif)
                .moyenneTrimestre(myAvg)
                .build();
    }

    private Double avgForModule(List<Note> notes, UUID moduleId) {
        double avg = notes.stream()
                .filter(n -> n.getExamen() != null && n.getExamen().getModule() != null)
                .filter(n -> moduleId.equals(n.getExamen().getModule().getId()))
                .filter(n -> n.getValeur() != null)
                .mapToDouble(n -> n.getValeur().doubleValue())
                .average()
                .orElse(-1.0);
        return avg < 0 ? null : Math.round(avg * 10.0) / 10.0;
    }

    private String computeTendance(Double t1, Double t2, Double t3) {
        // tendance basée sur la pente moyenne — neutre si données manquantes
        double last = t3 != null ? t3 : (t2 != null ? t2 : (t1 != null ? t1 : 0));
        double base = t1 != null && t2 != null ? (t1 + t2) / 2.0 : (t1 != null ? t1 : (t2 != null ? t2 : 0));
        if (base == 0 || last == 0) return "stable";
        if (last > base + 0.5) return "haut";
        if (last < base - 0.5) return "bas";
        return "stable";
    }

    private Student findChildOrThrow(UUID parentUserId, UUID studentId) {
        return parentStudentRepository.findByParentUserId(parentUserId).stream()
                .map(ParentStudent::getStudent)
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private UUID parseClasseId(String classeStr) {
        if (classeStr == null || classeStr.isBlank()) return null;
        try {
            return UUID.fromString(classeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private UpcomingDTO.UpcomingDevoir toUpcomingDevoir(Devoir d) {
        String moduleNom = moduleRepository.findById(d.getModuleId())
                .map(Module::getName)
                .orElse(null);
        return UpcomingDTO.UpcomingDevoir.builder()
                .id(d.getId())
                .titre(d.getTitre())
                .moduleNom(moduleNom)
                .dateLimite(d.getDateLimite())
                .type(d.getType())
                .build();
    }

    private UpcomingDTO.UpcomingExamen toUpcomingExamen(Examen e) {
        return UpcomingDTO.UpcomingExamen.builder()
                .id(e.getId())
                .name(e.getName())
                .moduleNom(e.getModule() != null ? e.getModule().getName() : null)
                .trimestre(e.getTrimestre())
                .dateLimiteSaisie(e.getDateLimiteSaisie())
                .build();
    }

    private UpcomingDTO.UpcomingPaiement toUpcomingPaiement(Paiement p) {
        return UpcomingDTO.UpcomingPaiement.builder()
                .id(p.getId())
                .typeFraisNom(p.getTypeFrais() != null ? p.getTypeFrais().getNom() : null)
                .mois(p.getMois())
                .anneeScolaire(p.getAnneeScolaire())
                .montantDu(p.getMontantDu())
                .montantPaye(p.getMontantPaye())
                .statut(p.getStatut() != null ? p.getStatut().name() : null)
                .build();
    }

    private void verifyParentLink(UUID parentUserId, UUID studentId) {
        boolean linked = parentStudentRepository.findByParentUserId(parentUserId)
                .stream()
                .anyMatch(ps -> ps.getStudent().getId().equals(studentId));
        if (!linked) {
            throw new ResourceNotFoundException("Student not linked to this parent account");
        }
    }

    private AbsenceResponseDTO toAbsenceDto(Absence a) {
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

    private EmploiDuTempsResponseDTO toEmploiDto(EmploiDuTemps e) {
        return EmploiDuTempsResponseDTO.builder()
                .id(e.getId())
                .classeId(e.getClasseId())
                .creneauId(e.getCreneauId())
                .jourSemaine(e.getJourSemaine())
                .moduleId(e.getModuleId())
                .enseignantId(e.getEnseignantId())
                .salle(e.getSalle())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
