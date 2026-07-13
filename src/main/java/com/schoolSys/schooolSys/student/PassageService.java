package com.schoolSys.schooolSys.student;

import java.util.UUID;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.niveau.Niveau;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import com.schoolSys.schooolSys.student.dto.PassageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassageService {

    private static final Pattern LEADING_DIGITS = Pattern.compile("(\\d+)");

    private final AnneeScolaireProvider anneeScolaireProvider;
    private final PassageRepository passageRepository;
    private final StudentRepository studentRepository;
    private final NiveauRepository niveauRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;

    public List<PassageDTO> findByAnneeScolaire(String anneeScolaire) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return passageRepository.findByAnneeScolaire(annee).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<PassageDTO> findByStudentId(UUID studentId) {
        return passageRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public PassageDTO create(PassageDTO dto) {
        if (!studentRepository.existsById(dto.getStudentId())) {
            throw new ResourceNotFoundException("Student", dto.getStudentId());
        }

        validateDecision(dto);
        assertNoExistingPassage(dto);
        assertAnneeOuverte(dto);

        Passage passage = Passage.builder()
                .studentId(dto.getStudentId())
                .ancienNiveau(dto.getAncienNiveau())
                .nouveauNiveau(dto.getNouveauNiveau())
                .ancienneClasse(dto.getAncienneClasse())
                .nouvelleClasse(dto.getNouvelleClasse())
                .decision(dto.getDecision())
                .anneeScolaire(dto.getAnneeScolaire())
                .motif(dto.getMotif())
                .build();

        // ANN-003: attach the AnneeScolaire row when its label matches.
        if (dto.getAnneeScolaire() != null) {
            anneeScolaireRepository.findByLabel(dto.getAnneeScolaire())
                    .ifPresent(passage::setAnneeScolaireRef);
        }

        Passage saved = passageRepository.save(passage);

        // Update student's niveau and classe if PASSAGE
        if ("PASSAGE".equals(dto.getDecision()) && dto.getNouveauNiveau() != null) {
            studentRepository.findById(dto.getStudentId()).ifPresent(student -> {
                student.setNiveau(dto.getNouveauNiveau());
                if (dto.getNouvelleClasse() != null) {
                    student.setClasse(dto.getNouvelleClasse());
                }
                studentRepository.save(student);
            });
        }

        return toDTO(saved);
    }

    @Transactional
    public List<PassageDTO> bulkCreate(List<PassageDTO> dtos) {
        return dtos.stream()
                .map(this::create)
                .toList();
    }

    /**
     * ANN-015: a student may only receive one end-of-year decision per school year.
     */
    private void assertNoExistingPassage(PassageDTO dto) {
        if (dto.getAnneeScolaire() == null || dto.getAnneeScolaire().isBlank()) return;
        boolean exists = passageRepository.findByStudentId(dto.getStudentId()).stream()
                .anyMatch(p -> dto.getAnneeScolaire().equalsIgnoreCase(p.getAnneeScolaire()));
        if (exists) {
            throw new IllegalArgumentException(
                    "Une décision a déjà été enregistrée pour cet élève sur l'année « "
                            + dto.getAnneeScolaire() + " ».");
        }
    }

    /** ANN-032: no decision may be recorded against a clôturée school year. */
    private void assertAnneeOuverte(PassageDTO dto) {
        if (dto.getAnneeScolaire() == null || dto.getAnneeScolaire().isBlank()) return;
        anneeScolaireRepository.findByLabel(dto.getAnneeScolaire())
                .filter(a -> Boolean.TRUE.equals(a.getCloturee()))
                .ifPresent(a -> {
                    throw new IllegalStateException("L'année scolaire « " + dto.getAnneeScolaire()
                            + " » est clôturée — aucune nouvelle décision possible.");
                });
    }

    /**
     * Enforces business rules on the decision:
     * - PASSAGE: nouveauNiveau must be exactly the next niveau in the configured order
     *   (leading number + 1). Skipping a level is rejected.
     * - REDOUBLEMENT: nouveauNiveau, when provided, must equal ancienNiveau (the student
     *   stays in the same niveau; only the classe section may change).
     *
     * EXCLUSION and TRANSFERT are not constrained here — they intentionally bypass the
     * progression rules.
     */
    private void validateDecision(PassageDTO dto) {
        String decision = dto.getDecision() == null ? "" : dto.getDecision().toUpperCase();
        String ancien = dto.getAncienNiveau();
        String nouveau = dto.getNouveauNiveau();

        // ANN-014: EXCLUSION and TRANSFERT must be justified.
        if (("EXCLUSION".equals(decision) || "TRANSFERT".equals(decision))
                && (dto.getMotif() == null || dto.getMotif().isBlank())) {
            throw new IllegalArgumentException(
                    "Le motif est obligatoire pour une décision « " + decision + " ».");
        }

        if ("PASSAGE".equals(decision)) {
            if (ancien == null || ancien.isBlank()) {
                throw new IllegalArgumentException("Passage impossible : le niveau actuel de l'élève est inconnu.");
            }
            if (nouveau == null || nouveau.isBlank()) {
                throw new IllegalArgumentException("Passage : le nouveau niveau est requis.");
            }
            String expected = nextNiveauName(ancien);
            if (expected == null) {
                throw new IllegalArgumentException(
                        "Passage impossible : aucun niveau supérieur n'est configuré après « " + ancien + " ».");
            }
            if (!expected.equalsIgnoreCase(nouveau.trim())) {
                throw new IllegalArgumentException(
                        "Passage invalide : un passage ne peut pas sauter de niveau. Depuis « "
                                + ancien + " » le seul passage autorisé est vers « " + expected + " ».");
            }
        } else if ("REDOUBLEMENT".equals(decision)) {
            if (ancien != null && nouveau != null && !nouveau.isBlank()
                    && !ancien.trim().equalsIgnoreCase(nouveau.trim())) {
                throw new IllegalArgumentException(
                        "Redoublement : le niveau doit rester « " + ancien + " ».");
            }
        }
    }

    /**
     * Returns the name of the niveau whose leading digit is exactly currentDigit + 1.
     * Returns null if the current niveau has no parseable digit, or if no niveau with
     * digit + 1 is configured.
     */
    private String nextNiveauName(String currentName) {
        Integer current = extractLeadingDigit(currentName);
        if (current == null) return null;
        int target = current + 1;
        return niveauRepository.findAll().stream()
                .filter(n -> {
                    Integer d = extractLeadingDigit(n.getName());
                    return d != null && d == target;
                })
                .map(Niveau::getName)
                .findFirst()
                .orElse(null);
    }

    private static Integer extractLeadingDigit(String name) {
        if (name == null) return null;
        Matcher m = LEADING_DIGITS.matcher(name);
        if (!m.find()) return null;
        try {
            return Integer.parseInt(m.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private PassageDTO toDTO(Passage passage) {
        String studentName = studentRepository.findById(passage.getStudentId())
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .orElse("Inconnu");

        return PassageDTO.builder()
                .id(passage.getId())
                .studentId(passage.getStudentId())
                .studentName(studentName)
                .ancienNiveau(passage.getAncienNiveau())
                .nouveauNiveau(passage.getNouveauNiveau())
                .ancienneClasse(passage.getAncienneClasse())
                .nouvelleClasse(passage.getNouvelleClasse())
                .decision(passage.getDecision())
                .anneeScolaire(passage.getAnneeScolaire())
                .motif(passage.getMotif())
                .createdAt(passage.getCreatedAt() != null ? passage.getCreatedAt().toString() : null)
                .build();
    }
}
