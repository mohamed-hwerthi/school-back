package com.schoolSys.schooolSys.scolarite;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.scolarite.dto.AttestationReussiteDTO;
import com.schoolSys.schooolSys.scolarite.dto.ReinscriptionResultDTO;
import com.schoolSys.schooolSys.scolarite.dto.ScolariteDTO;
import com.schoolSys.schooolSys.scolarite.dto.SuiviReinscriptionDTO;
import com.schoolSys.schooolSys.student.Passage;
import com.schoolSys.schooolSys.student.PassageRepository;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Historised schooling (ANN-004) and end-of-year re-enrolment (ANN-050/051):
 * tracks each student's niveau/classe per year and rolls passing students over.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScolariteService {

    private static final Pattern YEAR = Pattern.compile("(\\d{4})-(\\d{4})");

    private final ScolariteRepository scolariteRepository;
    private final StudentRepository studentRepository;
    private final PassageRepository passageRepository;

    /** ANN-004 — a student's year-by-year schooling history. */
    public List<ScolariteDTO> getHistorique(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", studentId);
        }
        String name = studentName(studentId);
        return scolariteRepository.findByStudentIdOrderByAnneeScolaireDesc(studentId).stream()
                .map(s -> toDTO(s, name))
                .toList();
    }

    /**
     * ANN-050 — re-enrol every student with a PASSAGE decision in {@code anneeSource}
     * into {@code anneeDestination}, at their new niveau/classe.
     */
    @Transactional
    public ReinscriptionResultDTO reinscrire(String anneeSource, String anneeDestination) {
        if (anneeSource == null || anneeSource.isBlank()
                || anneeDestination == null || anneeDestination.isBlank()) {
            throw new IllegalArgumentException("Les années source et destination sont requises.");
        }
        String source = anneeSource.trim();
        String dest = anneeDestination.trim();

        List<Passage> passages = passageRepository.findByAnneeScolaireAndDecision(source, "PASSAGE");
        int reinscrits = 0;
        int ignores = 0;
        for (Passage p : passages) {
            if (scolariteRepository.existsByStudentIdAndAnneeScolaire(p.getStudentId(), dest)) {
                ignores++;
                continue;
            }
            scolariteRepository.save(Scolarite.builder()
                    .studentId(p.getStudentId())
                    .anneeScolaire(dest)
                    .niveau(p.getNouveauNiveau())
                    .classe(p.getNouvelleClasse())
                    .statut("REINSCRIT")
                    .build());
            reinscrits++;
        }
        return ReinscriptionResultDTO.builder()
                .anneeSource(source)
                .anneeDestination(dest)
                .nbReinscrits(reinscrits)
                .nbIgnores(ignores)
                .message(reinscrits + " élève(s) réinscrit(s) en " + dest
                        + (ignores > 0 ? " — " + ignores + " déjà inscrit(s)" : "") + ".")
                .build();
    }

    /** ANN-051 — re-enrolment tracking: this year vs the previous one. */
    public SuiviReinscriptionDTO getSuivi(String anneeScolaire) {
        String annee = anneeScolaire.trim();
        String precedente = previousLabel(annee);

        Set<UUID> actuels = scolariteRepository.findByAnneeScolaire(annee).stream()
                .map(Scolarite::getStudentId)
                .collect(Collectors.toSet());
        Set<UUID> precedents = precedente == null ? Set.of()
                : scolariteRepository.findByAnneeScolaire(precedente).stream()
                        .map(Scolarite::getStudentId)
                        .collect(Collectors.toSet());

        int reinscrits = (int) actuels.stream().filter(precedents::contains).count();
        int partis = (int) precedents.stream().filter(id -> !actuels.contains(id)).count();

        return SuiviReinscriptionDTO.builder()
                .anneeScolaire(annee)
                .anneePrecedente(precedente)
                .totalInscrits(actuels.size())
                .reinscrits(reinscrits)
                .nouveaux(actuels.size() - reinscrits)
                .partis(partis)
                .build();
    }

    /** ANN-042 — certificate of success: did the student pass that year? */
    public AttestationReussiteDTO getAttestationReussite(UUID studentId, String anneeScolaire) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        String annee = anneeScolaire.trim();
        String name = student.getFirstName() + " " + student.getLastName();

        Optional<Passage> passage = passageRepository.findByStudentId(studentId).stream()
                .filter(p -> annee.equalsIgnoreCase(p.getAnneeScolaire())
                        && "PASSAGE".equalsIgnoreCase(p.getDecision()))
                .findFirst();

        if (passage.isEmpty()) {
            return AttestationReussiteDTO.builder()
                    .studentId(studentId).studentName(name).anneeScolaire(annee)
                    .eligible(false)
                    .message("Aucune décision de passage enregistrée pour cet élève en " + annee + ".")
                    .build();
        }
        Passage p = passage.get();
        return AttestationReussiteDTO.builder()
                .studentId(studentId).studentName(name).anneeScolaire(annee)
                .eligible(true)
                .ancienNiveau(p.getAncienNiveau())
                .nouveauNiveau(p.getNouveauNiveau())
                .decision(p.getDecision())
                .message("Admis(e) en classe supérieure" + (p.getNouveauNiveau() != null
                        ? " : " + p.getNouveauNiveau() : "") + ".")
                .build();
    }

    private ScolariteDTO toDTO(Scolarite s, String studentName) {
        return ScolariteDTO.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .studentName(studentName)
                .anneeScolaire(s.getAnneeScolaire())
                .niveau(s.getNiveau())
                .classe(s.getClasse())
                .statut(s.getStatut())
                .build();
    }

    private String studentName(UUID studentId) {
        return studentRepository.findById(studentId)
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .orElse("Inconnu");
    }

    /** "2025-2026" -> "2024-2025"; null when the label is not a YYYY-YYYY range. */
    private String previousLabel(String label) {
        if (label == null) return null;
        Matcher m = YEAR.matcher(label);
        if (!m.matches()) return null;
        return (Integer.parseInt(m.group(1)) - 1) + "-" + (Integer.parseInt(m.group(2)) - 1);
    }
}
