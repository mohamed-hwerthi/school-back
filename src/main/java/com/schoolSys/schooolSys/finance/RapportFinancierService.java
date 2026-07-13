package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.depense.Depense;
import com.schoolSys.schooolSys.depense.DepenseRepository;
import com.schoolSys.schooolSys.finance.dto.RapportFinancierDTO;
import com.schoolSys.schooolSys.finance.dto.RapportFinancierDTO.*;
import com.schoolSys.schooolSys.student.Student;
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
public class RapportFinancierService {

    private final PaiementRepository paiementRepository;
    private final DepenseRepository depenseRepository;
    private final RemiseRepository remiseRepository;
    private final PenaliteRepository penaliteRepository;
    private final RelanceRepository relanceRepository;

    private static final List<String> MOIS_ORDER = List.of(
            "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun"
    );

    public RapportFinancierDTO generer(String anneeScolaire) {
        List<Paiement> paiements = paiementRepository.findByAnneeScolaire(anneeScolaire);

        return RapportFinancierDTO.builder()
                .recapitulatif(buildRecapitulatif(paiements, anneeScolaire))
                .parMois(buildParMois(paiements, anneeScolaire))
                .parClasse(buildParClasse(paiements))
                .parEleve(buildParEleve(paiements))
                .build();
    }

    // ── Recapitulatif ──

    private Recapitulatif buildRecapitulatif(List<Paiement> paiements, String annee) {
        BigDecimal totalDu = BigDecimal.ZERO;
        BigDecimal totalPaye = BigDecimal.ZERO;
        long nbPayes = 0, nbPartiels = 0, nbEnRetard = 0, nbEnAttente = 0;

        for (Paiement p : paiements) {
            totalDu = totalDu.add(p.getMontantDu());
            totalPaye = totalPaye.add(p.getMontantPaye());
            switch (p.getStatut()) {
                case PAYE -> nbPayes++;
                case PARTIEL -> nbPartiels++;
                case EN_RETARD -> nbEnRetard++;
                case EN_ATTENTE -> nbEnAttente++;
            }
        }

        BigDecimal totalImpayes = totalDu.subtract(totalPaye);
        BigDecimal totalDepenses = depenseRepository.sumMontantByAnneeScolaire(annee);
        BigDecimal soldeNet = totalPaye.subtract(totalDepenses);
        BigDecimal taux = totalDu.compareTo(BigDecimal.ZERO) > 0
                ? totalPaye.multiply(BigDecimal.valueOf(100)).divide(totalDu, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalRemises = remiseRepository.sumRemisesFixesByAnneeScolaire(annee);
        BigDecimal totalPenalites = penaliteRepository.sumMontantByAnneeScolaire(annee);
        long nbRelances = relanceRepository.countByAnneeScolaire(annee);

        return Recapitulatif.builder()
                .totalDu(totalDu)
                .totalPaye(totalPaye)
                .totalImpayes(totalImpayes)
                .totalDepenses(totalDepenses)
                .soldeNet(soldeNet)
                .tauxRecouvrement(taux)
                .nbPaiements(paiements.size())
                .nbPayes(nbPayes)
                .nbPartiels(nbPartiels)
                .nbEnRetard(nbEnRetard)
                .nbEnAttente(nbEnAttente)
                .totalRemises(totalRemises != null ? totalRemises : BigDecimal.ZERO)
                .totalPenalites(totalPenalites != null ? totalPenalites : BigDecimal.ZERO)
                .nbRelances(nbRelances)
                .build();
    }

    // ── Par mois ──

    private List<LigneParMois> buildParMois(List<Paiement> paiements, String annee) {
        Map<String, List<Paiement>> byMois = paiements.stream()
                .collect(Collectors.groupingBy(Paiement::getMois));

        // Compute expenses by month from date
        List<Depense> depenses = depenseRepository.findByAnneeScolaire(annee);
        Map<String, BigDecimal> depenseParMois = depenses.stream()
                .collect(Collectors.groupingBy(
                        d -> monthAbbrev(d.getDateDepense().getMonthValue()),
                        Collectors.reducing(BigDecimal.ZERO, Depense::getMontant, BigDecimal::add)
                ));

        return MOIS_ORDER.stream().map(mois -> {
            List<Paiement> list = byMois.getOrDefault(mois, List.of());
            BigDecimal du = list.stream().map(Paiement::getMontantDu).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paye = list.stream().map(Paiement::getMontantPaye).reduce(BigDecimal.ZERO, BigDecimal::add);

            return LigneParMois.builder()
                    .mois(mois)
                    .montantDu(du)
                    .montantPaye(paye)
                    .solde(du.subtract(paye))
                    .nbPaiements(list.size())
                    .depenses(depenseParMois.getOrDefault(mois, BigDecimal.ZERO))
                    .build();
        }).toList();
    }

    private String monthAbbrev(int month) {
        return switch (month) {
            case 9 -> "Sep"; case 10 -> "Oct"; case 11 -> "Nov"; case 12 -> "Dec";
            case 1 -> "Jan"; case 2 -> "Feb"; case 3 -> "Mar"; case 4 -> "Apr";
            case 5 -> "May"; case 6 -> "Jun"; default -> "?";
        };
    }

    // ── Par classe ──

    private List<LigneParClasse> buildParClasse(List<Paiement> paiements) {
        Map<String, List<Paiement>> byClasse = paiements.stream()
                .filter(p -> p.getStudent().getClasse() != null)
                .collect(Collectors.groupingBy(p -> p.getStudent().getClasse()));

        return byClasse.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    String classe = e.getKey();
                    List<Paiement> list = e.getValue();
                    BigDecimal du = list.stream().map(Paiement::getMontantDu).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal paye = list.stream().map(Paiement::getMontantPaye).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long nbEleves = list.stream().map(p -> p.getStudent().getId()).distinct().count();
                    BigDecimal taux = du.compareTo(BigDecimal.ZERO) > 0
                            ? paye.multiply(BigDecimal.valueOf(100)).divide(du, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return LigneParClasse.builder()
                            .classe(classe)
                            .nbEleves(nbEleves)
                            .montantDu(du)
                            .montantPaye(paye)
                            .solde(du.subtract(paye))
                            .tauxRecouvrement(taux)
                            .build();
                }).toList();
    }

    // ── Par eleve ──

    private List<LigneParEleve> buildParEleve(List<Paiement> paiements) {
        Map<UUID, List<Paiement>> byStudent = paiements.stream()
                .collect(Collectors.groupingBy(p -> p.getStudent().getId()));

        return byStudent.entrySet().stream()
                .map(e -> {
                    List<Paiement> list = e.getValue();
                    Student student = list.get(0).getStudent();
                    BigDecimal du = list.stream().map(Paiement::getMontantDu).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal paye = list.stream().map(Paiement::getMontantPaye).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal solde = du.subtract(paye);
                    String statut = solde.compareTo(BigDecimal.ZERO) <= 0 ? "A_JOUR" : "EN_RETARD";
                    return LigneParEleve.builder()
                            .studentId(student.getId())
                            .nom(student.getLastName())
                            .prenom(student.getFirstName())
                            .classe(student.getClasse())
                            .montantDu(du)
                            .montantPaye(paye)
                            .solde(solde)
                            .statut(statut)
                            .build();
                })
                .sorted(Comparator.comparing(LigneParEleve::getSolde).reversed())
                .toList();
    }
}
