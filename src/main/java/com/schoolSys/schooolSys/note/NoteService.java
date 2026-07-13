package com.schoolSys.schooolSys.note;

import java.util.UUID;

import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.examen.Examen;
import com.schoolSys.schooolSys.examen.ExamenRepository;
import com.schoolSys.schooolSys.note.dto.*;
import com.schoolSys.schooolSys.parentnotif.ParentNotificationService;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository noteRepository;
    private final ExamenRepository examenRepository;
    private final StudentRepository studentRepository;
    private final BaremeRepository baremeRepository;
    private final CompetenceRepository competenceRepository;
    private final EvaluationCompetenceRepository evaluationCompetenceRepository;
    private final ParentNotificationService parentNotificationService;
    private final AnneeScolaireProvider anneeScolaireProvider;
    private final CurrentUserContext currentUser;
    private final AuditService auditService;

    public List<NoteResponseDTO> findByExamen(UUID examenId, Integer trimestre, String anneeScolaire) {
        String resolved = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return noteRepository.findByExamenIdAndTrimestreAndAnneeScolaire(examenId, trimestre, resolved).stream()
                .filter(n -> isInScope(n.getStudent().getId()))
                .map(this::toResponse).toList();
    }

    public List<NoteResponseDTO> findByStudent(UUID studentId, Integer trimestre, String anneeScolaire) {
        currentUser.assertCanAccessStudent(studentId);
        String resolved = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return noteRepository.findByStudentIdAndTrimestreAndAnneeScolaire(studentId, trimestre, resolved).stream()
                .map(this::toResponse).toList();
    }

    /** Silent filter for list endpoints — filtered out rows are simply not returned. */
    private boolean isInScope(UUID studentId) {
        try {
            currentUser.assertCanAccessStudent(studentId);
            return true;
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return false;
        }
    }

    @Transactional
    public List<NoteResponseDTO> upsertBulk(List<NoteRequestDTO> dtos) {
        // Block writes on students outside the current user's scope.
        for (NoteRequestDTO dto : dtos) {
            currentUser.assertCanAccessStudent(dto.getStudentId());
        }
        List<Note> saved = new ArrayList<>();
        for (NoteRequestDTO dto : dtos) {
            // NOT-008: Check deadline
            Examen examen = examenRepository.findById(dto.getExamenId())
                    .orElseThrow(() -> new ResourceNotFoundException("Examen", dto.getExamenId()));
            if (examen.getDateLimiteSaisie() != null && LocalDate.now().isAfter(examen.getDateLimiteSaisie())) {
                throw new IllegalStateException("La saisie des notes est verrouillée pour l'examen: " + examen.getName());
            }

            // NOT-007: Validate against barème
            baremeRepository.findByActifTrue().ifPresent(bareme -> {
                if (dto.getValeur() != null) {
                    double val = dto.getValeur();
                    if (val < bareme.getNoteMin().doubleValue() || val > bareme.getNoteMax().doubleValue()) {
                        throw new IllegalArgumentException("Note " + val + " hors barème [" +
                                bareme.getNoteMin() + ", " + bareme.getNoteMax() + "]");
                    }
                }
            });

            Optional<Note> existing = noteRepository.findByStudentIdAndExamenIdAndTrimestreAndAnneeScolaire(
                    dto.getStudentId(), dto.getExamenId(), dto.getTrimestre(), examen.getAnneeScolaire());

            String statut = dto.getStatut() != null ? dto.getStatut() : "PRESENT";
            // ABSENT => note forcée à 0 (compte dans la moyenne comme 0)
            Double valeur = "ABSENT".equals(statut) ? 0.0 : dto.getValeur();

            if (existing.isPresent()) {
                Note note = existing.get();
                note.setValeur(valeur);
                note.setObservation(dto.getObservation());
                note.setStatut(statut);
                note.setUpdatedAt(LocalDateTime.now());
                saved.add(noteRepository.save(note));
            } else {
                Student student = studentRepository.findById(dto.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
                Note note = Note.builder()
                        .student(student).examen(examen).trimestre(dto.getTrimestre())
                        .anneeScolaire(examen.getAnneeScolaire())
                        .valeur(valeur).observation(dto.getObservation())
                        .statut(statut).build();
                saved.add(noteRepository.save(note));
            }
        }
        // Trigger async notification parent — best effort, n'interrompt pas la sauvegarde si échec
        for (Note n : saved) {
            try {
                parentNotificationService.notifyNoteSaved(n.getId());
            } catch (Exception ignored) {
                // déjà loggé côté ParentNotificationService
            }
        }
        return saved.stream().map(this::toResponse).toList();
    }

    public List<MoyenneDTO> getMoyennes(UUID classeId, Integer trimestre, String anneeScolaire) {
        String resolved = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        List<Note> notes = noteRepository.findByExamenClasseIdAndTrimestreAndAnneeScolaire(classeId, trimestre, resolved);
        Map<UUID, List<Note>> byStudent = notes.stream()
                .collect(Collectors.groupingBy(n -> n.getStudent().getId()));

        List<MoyenneDTO> result = new ArrayList<>();
        for (Map.Entry<UUID, List<Note>> entry : byStudent.entrySet()) {
            List<Note> studentNotes = entry.getValue();
            Student student = studentNotes.get(0).getStudent();
            Map<String, List<Note>> byModule = studentNotes.stream()
                    .collect(Collectors.groupingBy(n -> n.getExamen().getModule().getName()));

            Map<String, Double> moyennesParModule = new LinkedHashMap<>();
            double totalWeighted = 0, totalCoeff = 0;

            for (Map.Entry<String, List<Note>> moduleEntry : byModule.entrySet()) {
                List<Note> moduleNotes = moduleEntry.getValue();
                double sumWeighted = 0, sumCoeff = 0;
                for (Note n : moduleNotes) {
                    if (n.getValeur() != null) {
                        double coeff = n.getExamen().getCoeffEtatique();
                        sumWeighted += n.getValeur() * coeff;
                        sumCoeff += coeff;
                    }
                }
                double moyenne = sumCoeff > 0 ? Math.round(sumWeighted / sumCoeff * 100.0) / 100.0 : 0;
                moyennesParModule.put(moduleEntry.getKey(), moyenne);
                double moduleCoeff = moduleNotes.get(0).getExamen().getModule().getCoeffEtatique();
                totalWeighted += moyenne * moduleCoeff;
                totalCoeff += moduleCoeff;
            }
            double moyenneGenerale = totalCoeff > 0 ? Math.round(totalWeighted / totalCoeff * 100.0) / 100.0 : 0;

            result.add(MoyenneDTO.builder()
                    .studentId(student.getId())
                    .studentName(student.getFirstName() + " " + student.getLastName())
                    .trimestre(trimestre)
                    .moyennesParModule(moyennesParModule)
                    .moyenneGenerale(moyenneGenerale)
                    .build());
        }
        result.sort(Comparator.comparing(MoyenneDTO::getStudentName));
        return result;
    }

    @Transactional
    public void delete(UUID id) {
        if (!noteRepository.existsById(id)) throw new ResourceNotFoundException("Note", id);
        noteRepository.deleteById(id);
        auditService.log("DELETE", "NOTE", id, "Suppression d'une note");
    }

    // --- Barèmes ---
    public List<BaremeDTO> getAllBaremes() {
        return baremeRepository.findAll().stream().map(b -> BaremeDTO.builder()
                .id(b.getId()).label(b.getLabel()).noteMax(b.getNoteMax())
                .noteMin(b.getNoteMin()).notePassage(b.getNotePassage()).actif(b.getActif()).build()).toList();
    }

    @Transactional
    public BaremeDTO createBareme(BaremeDTO dto) {
        Bareme b = Bareme.builder().label(dto.getLabel()).noteMax(dto.getNoteMax())
                .noteMin(dto.getNoteMin()).notePassage(dto.getNotePassage()).actif(dto.getActif()).build();
        b = baremeRepository.save(b);
        return BaremeDTO.builder().id(b.getId()).label(b.getLabel()).noteMax(b.getNoteMax())
                .noteMin(b.getNoteMin()).notePassage(b.getNotePassage()).actif(b.getActif()).build();
    }

    @Transactional
    public BaremeDTO updateBareme(UUID id, BaremeDTO dto) {
        Bareme b = baremeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bareme", id));
        b.setLabel(dto.getLabel()); b.setNoteMax(dto.getNoteMax()); b.setNoteMin(dto.getNoteMin());
        b.setNotePassage(dto.getNotePassage()); b.setActif(dto.getActif());
        b = baremeRepository.save(b);
        return BaremeDTO.builder().id(b.getId()).label(b.getLabel()).noteMax(b.getNoteMax())
                .noteMin(b.getNoteMin()).notePassage(b.getNotePassage()).actif(b.getActif()).build();
    }

    // --- Compétences ---
    public List<CompetenceDTO> getCompetences(UUID moduleId) {
        List<Competence> list = moduleId != null ? competenceRepository.findByModuleId(moduleId) : competenceRepository.findAll();
        return list.stream().map(c -> CompetenceDTO.builder()
                .id(c.getId()).moduleId(c.getModuleId()).label(c.getLabel()).description(c.getDescription()).build()).toList();
    }

    @Transactional
    public CompetenceDTO createCompetence(CompetenceDTO dto) {
        Competence c = Competence.builder().moduleId(dto.getModuleId()).label(dto.getLabel()).description(dto.getDescription()).build();
        c = competenceRepository.save(c);
        return CompetenceDTO.builder().id(c.getId()).moduleId(c.getModuleId()).label(c.getLabel()).description(c.getDescription()).build();
    }

    @Transactional
    public void deleteCompetence(UUID id) {
        if (!competenceRepository.existsById(id)) throw new ResourceNotFoundException("Competence", id);
        competenceRepository.deleteById(id);
    }

    public List<EvaluationCompetenceDTO> getEvaluationsCompetences(UUID examenId, UUID eleveId) {
        List<EvaluationCompetence> list = eleveId != null ?
                evaluationCompetenceRepository.findByEleveIdAndExamenId(eleveId, examenId) :
                evaluationCompetenceRepository.findByExamenId(examenId);
        return list.stream().map(e -> {
            String label = competenceRepository.findById(e.getCompetenceId()).map(Competence::getLabel).orElse("");
            return EvaluationCompetenceDTO.builder()
                    .id(e.getId()).eleveId(e.getEleveId()).competenceId(e.getCompetenceId())
                    .competenceLabel(label).examenId(e.getExamenId()).niveau(e.getNiveau())
                    .commentaire(e.getCommentaire()).build();
        }).toList();
    }

    @Transactional
    public EvaluationCompetenceDTO createEvaluationCompetence(EvaluationCompetenceDTO dto) {
        EvaluationCompetence e = EvaluationCompetence.builder()
                .eleveId(dto.getEleveId()).competenceId(dto.getCompetenceId())
                .examenId(dto.getExamenId()).niveau(dto.getNiveau()).commentaire(dto.getCommentaire()).build();
        e = evaluationCompetenceRepository.save(e);
        return EvaluationCompetenceDTO.builder().id(e.getId()).eleveId(e.getEleveId())
                .competenceId(e.getCompetenceId()).examenId(e.getExamenId())
                .niveau(e.getNiveau()).commentaire(e.getCommentaire()).build();
    }

    private NoteResponseDTO toResponse(Note note) {
        return NoteResponseDTO.builder()
                .id(note.getId()).studentId(note.getStudent().getId())
                .studentName(note.getStudent().getFirstName() + " " + note.getStudent().getLastName())
                .examenId(note.getExamen().getId()).examenName(note.getExamen().getName())
                .trimestre(note.getTrimestre()).valeur(note.getValeur()).observation(note.getObservation())
                .statut(note.getStatut()).build();
    }
}
