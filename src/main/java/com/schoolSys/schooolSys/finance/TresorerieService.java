package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.depense.CategorieDepenseRepository;
import com.schoolSys.schooolSys.depense.Depense;
import com.schoolSys.schooolSys.depense.DepenseRepository;
import com.schoolSys.schooolSys.finance.dto.TresorerieDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
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
public class TresorerieService {

    private final PaiementRepository paiementRepository;
    private final DepenseRepository depenseRepository;
    private final CategorieDepenseRepository categorieDepenseRepository;
    private final StudentRepository studentRepository;

    private static final List<String> MOIS_ORDER = List.of(
            "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun"
    );

    public TresorerieDTO getTresorerie(String anneeScolaire) {
        // ─── Totaux entrées ─────────────────────────────
        BigDecimal totalEntrees = paiementRepository.sumMontantPayeByAnneeScolaire(anneeScolaire);
        BigDecimal totalDu = paiementRepository.sumMontantDuByAnneeScolaire(anneeScolaire);
        BigDecimal totalImpayes = paiementRepository.sumImpayes(anneeScolaire);

        // ─── Totaux sorties ─────────────────────────────
        BigDecimal totalSorties = depenseRepository.sumMontantByAnneeScolaire(anneeScolaire);

        // ─── Solde & taux ───────────────────────────────
        BigDecimal solde = totalEntrees.subtract(totalSorties);
        BigDecimal tauxRecouvrement = totalDu.compareTo(BigDecimal.ZERO) > 0
                ? totalEntrees.multiply(BigDecimal.valueOf(100)).divide(totalDu, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ─── Élèves à jour / en retard ─────────────────
        List<Paiement> allPaiements = paiementRepository.findByAnneeScolaire(anneeScolaire);
        Map<UUID, BigDecimal[]> eleveSoldes = new HashMap<>();
        for (Paiement p : allPaiements) {
            eleveSoldes.computeIfAbsent(p.getStudent().getId(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] vals = eleveSoldes.get(p.getStudent().getId());
            vals[0] = vals[0].add(p.getMontantDu());
            vals[1] = vals[1].add(p.getMontantPaye());
        }
        long elevesAJour = eleveSoldes.values().stream()
                .filter(v -> v[1].compareTo(v[0]) >= 0)
                .count();
        long elevesEnRetard = eleveSoldes.size() - elevesAJour;

        // ─── Flux mensuels ──────────────────────────────
        List<Depense> allDepenses = depenseRepository.findByAnneeScolaire(anneeScolaire);
        Map<String, BigDecimal> depenseParMois = allDepenses.stream()
                .collect(Collectors.groupingBy(
                        d -> monthAbbrev(d.getDateDepense().getMonthValue()),
                        Collectors.reducing(BigDecimal.ZERO, Depense::getMontant, BigDecimal::add)
                ));

        List<TresorerieDTO.FluxMensuel> fluxMensuels = MOIS_ORDER.stream()
                .map(mois -> {
                    BigDecimal entreesMois = paiementRepository.sumMontantPayeByMoisAndAnneeScolaire(mois, anneeScolaire);
                    BigDecimal sortiesMois = depenseParMois.getOrDefault(mois, BigDecimal.ZERO);
                    return TresorerieDTO.FluxMensuel.builder()
                            .mois(mois)
                            .entrees(entreesMois)
                            .sorties(sortiesMois)
                            .solde(entreesMois.subtract(sortiesMois))
                            .build();
                })
                .toList();

        // ─── Top 10 débiteurs ───────────────────────────
        List<TresorerieDTO.TopDebiteur> topDebiteurs = eleveSoldes.entrySet().stream()
                .filter(e -> e.getValue()[1].compareTo(e.getValue()[0]) < 0)
                .sorted((a, b) -> {
                    BigDecimal soldeA = a.getValue()[0].subtract(a.getValue()[1]);
                    BigDecimal soldeB = b.getValue()[0].subtract(b.getValue()[1]);
                    return soldeB.compareTo(soldeA);
                })
                .limit(10)
                .map(e -> {
                    Student student = studentRepository.findById(e.getKey()).orElse(null);
                    BigDecimal du = e.getValue()[0];
                    BigDecimal paye = e.getValue()[1];
                    return TresorerieDTO.TopDebiteur.builder()
                            .studentId(e.getKey())
                            .studentName(student != null ? student.getFirstName() + " " + student.getLastName() : "Inconnu")
                            .classe(student != null ? student.getClasse() : "")
                            .montantDu(du)
                            .montantPaye(paye)
                            .solde(du.subtract(paye))
                            .build();
                })
                .toList();

        // ─── Répartition dépenses par catégorie ─────────
        List<TresorerieDTO.RepartitionDepense> repartitionDepenses = categorieDepenseRepository.findAll().stream()
                .map(cat -> {
                    BigDecimal total = depenseRepository.sumMontantByCategorieAndAnneeScolaire(cat.getId(), anneeScolaire);
                    return TresorerieDTO.RepartitionDepense.builder()
                            .categorie(cat.getNom())
                            .montant(total)
                            .build();
                })
                .filter(r -> r.getMontant().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        return TresorerieDTO.builder()
                .totalEntrees(totalEntrees)
                .totalSorties(totalSorties)
                .solde(solde)
                .totalDu(totalDu)
                .totalImpayes(totalImpayes)
                .tauxRecouvrement(tauxRecouvrement)
                .elevesAJour(elevesAJour)
                .elevesEnRetard(elevesEnRetard)
                .totalEleves(eleveSoldes.size())
                .fluxMensuels(fluxMensuels)
                .topDebiteurs(topDebiteurs)
                .repartitionDepenses(repartitionDepenses)
                .build();
    }

    private String monthAbbrev(int month) {
        return switch (month) {
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            case 1 -> "Jan";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Apr";
            case 5 -> "May";
            case 6 -> "Jun";
            default -> "?";
        };
    }
}
