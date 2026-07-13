package com.schoolSys.schooolSys.cloture;

import java.util.UUID;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaire;
import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import com.schoolSys.schooolSys.anneescolaire.Trimestre;
import com.schoolSys.schooolSys.anneescolaire.TrimestreRepository;
import com.schoolSys.schooolSys.cloture.dto.ClotureRequestDTO;
import com.schoolSys.schooolSys.cloture.dto.ClotureResultDTO;
import com.schoolSys.schooolSys.cloture.dto.PreCheckDTO;
import com.schoolSys.schooolSys.cloture.dto.PreChecksResponseDTO;
import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.examen.ExamenRepository;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.student.PassageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Year-end closure workflow (ANN-030/031/033): pre-closure verifications,
 * then closing a school year and optionally rolling over to the next one.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClotureService {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final TrimestreRepository trimestreRepository;
    private final PassageRepository passageRepository;
    private final NoteRepository noteRepository;
    private final ExamenRepository examenRepository;
    private final AuditService auditService;

    /** ANN-031 — pre-closure checks for a school year. */
    public PreChecksResponseDTO getPreChecks(UUID anneeId) {
        AnneeScolaire annee = anneeScolaireRepository.findById(anneeId)
                .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", anneeId));

        List<PreCheckDTO> checks = new ArrayList<>();

        boolean dejaCloturee = Boolean.TRUE.equals(annee.getCloturee());
        checks.add(check("ANNEE_OUVERTE", "Année non clôturée", !dejaCloturee, true,
                dejaCloturee ? "Cette année est déjà clôturée." : "L'année est ouverte."));

        int nbTrimestres = trimestreRepository.findByAnneeScolaireId(anneeId).size();
        checks.add(check("TRIMESTRES", "Trimestres configurés", nbTrimestres == 3, false,
                nbTrimestres + " trimestre(s) configuré(s) sur 3."));

        long nbExamens = examenRepository.count();
        checks.add(check("EXAMENS", "Examens créés", nbExamens > 0, false,
                nbExamens + " examen(s) enregistré(s)."));

        long nbNotes = noteRepository.count();
        checks.add(check("NOTES", "Notes saisies", nbNotes > 0, false,
                nbNotes + " note(s) saisie(s)."));

        int nbPassages = passageRepository.findByAnneeScolaire(annee.getLabel()).size();
        checks.add(check("PASSAGES", "Décisions de passage enregistrées", nbPassages > 0, false,
                nbPassages + " décision(s) de fin d'année traitée(s)."));

        boolean cloturable = checks.stream()
                .filter(PreCheckDTO::isBlocking)
                .allMatch(PreCheckDTO::isOk);

        return PreChecksResponseDTO.builder()
                .anneeId(anneeId)
                .anneeScolaire(annee.getLabel())
                .cloturable(cloturable)
                .checks(checks)
                .build();
    }

    /** ANN-030/033 — close a school year, optionally creating the next one. */
    @Transactional
    public ClotureResultDTO cloturer(UUID anneeId, ClotureRequestDTO request) {
        AnneeScolaire annee = anneeScolaireRepository.findById(anneeId)
                .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", anneeId));
        if (Boolean.TRUE.equals(annee.getCloturee())) {
            throw new IllegalStateException("Cette année scolaire est déjà clôturée.");
        }

        annee.setCloturee(true);
        annee.setActive(false);
        anneeScolaireRepository.save(annee);

        UUID nouvelleId = null;
        String nouvelleLabel = null;
        int trimestresCrees = 0;

        if (request != null && request.isCreerAnneeSuivante()) {
            AnneeScolaire nouvelle = creerAnneeSuivante(request);
            nouvelleId = nouvelle.getId();
            nouvelleLabel = nouvelle.getLabel();
            trimestresCrees = nouvelle.getTrimestres().size();
        }

        String message = "Année « " + annee.getLabel() + " » clôturée."
                + (nouvelleLabel != null ? " Nouvelle année « " + nouvelleLabel + " » créée." : "");

        // ANN-034: journalise la clôture dans l'audit.
        auditService.log("CLOTURE_ANNEE", "AnneeScolaire", anneeId,
                "{\"anneeCloturee\":\"" + annee.getLabel() + "\""
                        + (nouvelleLabel != null ? ",\"nouvelleAnnee\":\"" + nouvelleLabel + "\"" : "")
                        + ",\"trimestresCrees\":" + trimestresCrees + "}");

        return ClotureResultDTO.builder()
                .anneeClotureeId(anneeId)
                .anneeClotureeLabel(annee.getLabel())
                .nouvelleAnneeId(nouvelleId)
                .nouvelleAnneeLabel(nouvelleLabel)
                .trimestresCrees(trimestresCrees)
                .message(message)
                .build();
    }

    private AnneeScolaire creerAnneeSuivante(ClotureRequestDTO request) {
        String label = request.getLabelAnneeSuivante() == null
                ? null : request.getLabelAnneeSuivante().trim();
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Le libellé de la nouvelle année est requis.");
        }
        if (anneeScolaireRepository.findByLabel(label).isPresent()) {
            throw new IllegalArgumentException("Une année scolaire « " + label + " » existe déjà.");
        }
        LocalDate debut = request.getDateDebutSuivante();
        LocalDate fin = request.getDateFinSuivante();
        if (debut == null || fin == null || !fin.isAfter(debut)) {
            throw new IllegalArgumentException(
                    "Les dates de la nouvelle année sont invalides (la fin doit suivre le début).");
        }

        if (request.isActiverAnneeSuivante()) {
            anneeScolaireRepository.findAll().forEach(a -> {
                if (Boolean.TRUE.equals(a.getActive())) {
                    a.setActive(false);
                    anneeScolaireRepository.save(a);
                }
            });
        }

        AnneeScolaire nouvelle = AnneeScolaire.builder()
                .label(label)
                .dateDebut(debut)
                .dateFin(fin)
                .active(request.isActiverAnneeSuivante())
                .cloturee(false)
                .build();

        for (int numero = 1; numero <= 3; numero++) {
            LocalDate[] range = trimestreRange(debut, fin, numero);
            nouvelle.getTrimestres().add(Trimestre.builder()
                    .anneeScolaire(nouvelle)
                    .numero(numero)
                    .label(numero + (numero == 1 ? "er" : "ème") + " trimestre")
                    .dateDebut(range[0])
                    .dateFin(range[1])
                    .saisieNotesOuverte(true)
                    .build());
        }

        return anneeScolaireRepository.save(nouvelle);
    }

    /** Splits [debut, fin] into three contiguous trimestre date ranges. */
    private LocalDate[] trimestreRange(LocalDate debut, LocalDate fin, int numero) {
        long totalDays = ChronoUnit.DAYS.between(debut, fin);
        long step = totalDays / 3;
        LocalDate start = numero == 1 ? debut : debut.plusDays(step * (numero - 1) + 1);
        LocalDate end = numero == 3 ? fin : debut.plusDays(step * numero);
        return new LocalDate[]{start, end};
    }

    private static PreCheckDTO check(String code, String label, boolean ok,
                                     boolean blocking, String detail) {
        return PreCheckDTO.builder()
                .code(code).label(label).ok(ok).blocking(blocking).detail(detail)
                .build();
    }
}
