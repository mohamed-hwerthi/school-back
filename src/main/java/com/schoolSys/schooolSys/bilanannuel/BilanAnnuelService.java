package com.schoolSys.schooolSys.bilanannuel;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaire;
import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import com.schoolSys.schooolSys.bilanannuel.dto.BilanAnnuelDTO;
import com.schoolSys.schooolSys.bilanannuel.dto.NiveauBilanDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.student.Passage;
import com.schoolSys.schooolSys.student.PassageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates end-of-year {@code Passage} decisions into the "Bilan annuel"
 * dashboard figures — globally and per niveau (ANN-021).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BilanAnnuelService {

    private static final String[] DECISIONS = {"PASSAGE", "REDOUBLEMENT", "EXCLUSION", "TRANSFERT"};

    private final PassageRepository passageRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;

    /**
     * Build the annual review for a school year. When {@code anneeScolaire} is
     * blank, the active academic year is used.
     */
    public BilanAnnuelDTO getBilan(String anneeScolaire) {
        String annee = (anneeScolaire != null && !anneeScolaire.isBlank())
                ? anneeScolaire.trim()
                : anneeScolaireRepository.findByActiveTrue()
                        .map(AnneeScolaire::getLabel)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Aucune année scolaire active n'est configurée."));

        List<Passage> passages = passageRepository.findByAnneeScolaire(annee);

        int[] global = new int[4];
        Map<String, int[]> parNiveau = new LinkedHashMap<>();

        for (Passage p : passages) {
            int idx = decisionIndex(p.getDecision());
            if (idx < 0) continue;
            global[idx]++;
            String niveau = (p.getAncienNiveau() != null && !p.getAncienNiveau().isBlank())
                    ? p.getAncienNiveau().trim()
                    : "Non renseigné";
            parNiveau.computeIfAbsent(niveau, k -> new int[4])[idx]++;
        }

        int total = global[0] + global[1] + global[2] + global[3];

        List<NiveauBilanDTO> niveauStats = new ArrayList<>();
        for (Map.Entry<String, int[]> e : parNiveau.entrySet()) {
            int[] c = e.getValue();
            int niveauTotal = c[0] + c[1] + c[2] + c[3];
            niveauStats.add(NiveauBilanDTO.builder()
                    .niveau(e.getKey())
                    .total(niveauTotal)
                    .nbPassage(c[0])
                    .nbRedoublement(c[1])
                    .nbExclusion(c[2])
                    .nbTransfert(c[3])
                    .tauxPassage(percent(c[0], niveauTotal))
                    .build());
        }
        niveauStats.sort((a, b) -> a.getNiveau().compareToIgnoreCase(b.getNiveau()));

        return BilanAnnuelDTO.builder()
                .anneeScolaire(annee)
                .totalDecisions(total)
                .nbPassage(global[0])
                .nbRedoublement(global[1])
                .nbExclusion(global[2])
                .nbTransfert(global[3])
                .tauxPassage(percent(global[0], total))
                .tauxRedoublement(percent(global[1], total))
                .tauxExclusion(percent(global[2], total))
                .tauxTransfert(percent(global[3], total))
                .parNiveau(niveauStats)
                .build();
    }

    /** ANN-023 — annual review for every recorded school year, oldest first. */
    public List<BilanAnnuelDTO> getComparatif() {
        return anneeScolaireRepository.findAll().stream()
                .sorted((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()))
                .map(a -> getBilan(a.getLabel()))
                .toList();
    }

    private static int decisionIndex(String decision) {
        if (decision == null) return -1;
        for (int i = 0; i < DECISIONS.length; i++) {
            if (DECISIONS[i].equalsIgnoreCase(decision.trim())) return i;
        }
        return -1;
    }

    private static double percent(int count, int total) {
        if (total == 0) return 0.0;
        return Math.round((double) count / total * 1000.0) / 10.0;
    }
}
