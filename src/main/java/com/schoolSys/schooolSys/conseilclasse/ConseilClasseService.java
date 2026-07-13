package com.schoolSys.schooolSys.conseilclasse;

import java.util.UUID;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import com.schoolSys.schooolSys.bulletin.BulletinService;
import com.schoolSys.schooolSys.bulletin.dto.BulletinDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.conseilclasse.dto.ConseilClasseDTO;
import com.schoolSys.schooolSys.conseilclasse.dto.PropositionPassageDTO;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.niveau.Niveau;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import com.schoolSys.schooolSys.note.BaremeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annual decision engine for the "conseil de classe" (ANN-001/002/010/011).
 *
 * <p>It computes each student's annual average — the mean of their three
 * trimestre general averages (reusing {@link BulletinService}) — and proposes
 * a {@code PASSAGE}/{@code REDOUBLEMENT} decision against the active barème's
 * pass threshold. The directeur reviews and validates these proposals before
 * they become {@code Passage} records.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConseilClasseService {

    private static final Pattern LEADING_DIGITS = Pattern.compile("(\\d+)");
    private static final double DEFAULT_SEUIL = 10.0;
    private static final String VERSION = "etatique";

    private final BulletinService bulletinService;
    private final ClasseRepository classeRepository;
    private final NiveauRepository niveauRepository;
    private final BaremeRepository baremeRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;

    /**
     * Build the conseil de classe for a class: annual averages, ranks and a
     * proposed decision for every student that has at least one grade.
     */
    public ConseilClasseDTO getConseilClasse(UUID classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe", classeId));

        String niveauNom = classe.getNiveau().getName();
        String niveauSuivant = nextNiveauName(niveauNom);

        double seuil = baremeRepository.findByActifTrue()
                .map(b -> b.getNotePassage().doubleValue())
                .orElse(DEFAULT_SEUIL);

        String anneeScolaire = anneeScolaireRepository.findByActiveTrue()
                .map(a -> a.getLabel())
                .orElse(null);

        // 1. Per-trimestre general averages (reuse the bulletin engine).
        Map<UUID, String> names = new LinkedHashMap<>();
        Map<UUID, String> classeDisplay = new LinkedHashMap<>();
        Map<UUID, Double> mt1 = new LinkedHashMap<>();
        Map<UUID, Double> mt2 = new LinkedHashMap<>();
        Map<UUID, Double> mt3 = new LinkedHashMap<>();

        index(bulletinService.getBulletins(classeId, 1, VERSION), names, classeDisplay, mt1);
        index(bulletinService.getBulletins(classeId, 2, VERSION), names, classeDisplay, mt2);
        index(bulletinService.getBulletins(classeId, 3, VERSION), names, classeDisplay, mt3);

        // 2. Build one proposition per student.
        List<PropositionPassageDTO> propositions = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : names.entrySet()) {
            UUID studentId = entry.getKey();
            Double t1 = mt1.get(studentId);
            Double t2 = mt2.get(studentId);
            Double t3 = mt3.get(studentId);

            Double annuelle = moyenneAnnuelle(t1, t2, t3);
            boolean passe = annuelle != null && annuelle >= seuil;

            propositions.add(PropositionPassageDTO.builder()
                    .studentId(studentId)
                    .studentName(entry.getValue())
                    .niveauActuel(niveauNom)
                    .classeActuelle(classeDisplay.get(studentId))
                    .moyenneT1(t1)
                    .moyenneT2(t2)
                    .moyenneT3(t3)
                    .moyenneAnnuelle(annuelle)
                    .decisionProposee(passe ? "PASSAGE" : "REDOUBLEMENT")
                    .niveauSuivant(niveauSuivant)
                    .mention(mention(annuelle))
                    .build());
        }

        // 3. Rank by annual average (descending, ties share a rank).
        assignRanks(propositions);

        // Stable, readable ordering: ranked students first, then by name.
        propositions.sort((a, b) -> {
            if (!Objects.equals(a.getRang(), b.getRang())) {
                if (a.getRang() == null) return 1;
                if (b.getRang() == null) return -1;
                return Integer.compare(a.getRang(), b.getRang());
            }
            return safe(a.getStudentName()).compareToIgnoreCase(safe(b.getStudentName()));
        });

        return ConseilClasseDTO.builder()
                .classeId(classeId)
                .classeNom(buildFullName(classe))
                .niveauNom(niveauNom)
                .niveauSuivant(niveauSuivant)
                .seuilPassage(seuil)
                .anneeScolaire(anneeScolaire)
                .propositions(propositions)
                .build();
    }

    /** Collect names, classe display and the trimestre average for each student. */
    private void index(List<BulletinDTO> bulletins, Map<UUID, String> names,
                       Map<UUID, String> classeDisplay, Map<UUID, Double> trimestre) {
        for (BulletinDTO b : bulletins) {
            names.putIfAbsent(b.getStudentId(), b.getStudentName());
            classeDisplay.putIfAbsent(b.getStudentId(), b.getClasse());
            if (b.getMoyenneGenerale() != null) {
                trimestre.put(b.getStudentId(), b.getMoyenneGenerale());
            }
        }
    }

    /** Mean of the available trimestre averages, or {@code null} when none exist. */
    private Double moyenneAnnuelle(Double t1, Double t2, Double t3) {
        double sum = 0;
        int count = 0;
        for (Double t : new Double[]{t1, t2, t3}) {
            if (t != null) {
                sum += t;
                count++;
            }
        }
        return count == 0 ? null : round2(sum / count);
    }

    /** Honour-roll mention ("tableau d'honneur") derived from the annual average (ANN-024). */
    private String mention(Double moyenne) {
        if (moyenne == null) return null;
        if (moyenne >= 18) return "Excellence";
        if (moyenne >= 16) return "Félicitations";
        if (moyenne >= 14) return "Tableau d'honneur";
        if (moyenne >= 12) return "Encouragements";
        return null;
    }

    /** Descending rank by annual average; equal averages share a rank, nulls stay unranked. */
    private void assignRanks(List<PropositionPassageDTO> propositions) {
        List<PropositionPassageDTO> ranked = propositions.stream()
                .filter(p -> p.getMoyenneAnnuelle() != null)
                .sorted((a, b) -> Double.compare(b.getMoyenneAnnuelle(), a.getMoyenneAnnuelle()))
                .toList();
        for (int i = 0; i < ranked.size(); i++) {
            if (i > 0 && Objects.equals(ranked.get(i).getMoyenneAnnuelle(),
                    ranked.get(i - 1).getMoyenneAnnuelle())) {
                ranked.get(i).setRang(ranked.get(i - 1).getRang());
            } else {
                ranked.get(i).setRang(i + 1);
            }
        }
    }

    /**
     * Name of the niveau whose leading digit is exactly currentDigit + 1.
     * Mirrors the progression rule enforced by {@code PassageService}.
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

    private String buildFullName(Classe classe) {
        String niveauName = classe.getNiveau().getName();
        StringBuilder digits = new StringBuilder();
        for (char ch : niveauName.toCharArray()) {
            if (Character.isDigit(ch)) digits.append(ch);
            else break;
        }
        return digits + classe.getLetter();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
