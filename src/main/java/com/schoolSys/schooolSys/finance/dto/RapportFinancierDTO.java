package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RapportFinancierDTO {

    private Recapitulatif recapitulatif;
    private List<LigneParMois> parMois;
    private List<LigneParClasse> parClasse;
    private List<LigneParEleve> parEleve;

    @Data
    @Builder
    public static class Recapitulatif {
        private BigDecimal totalDu;
        private BigDecimal totalPaye;
        private BigDecimal totalImpayes;
        private BigDecimal totalDepenses;
        private BigDecimal soldeNet;
        private BigDecimal tauxRecouvrement;
        private long nbPaiements;
        private long nbPayes;
        private long nbPartiels;
        private long nbEnRetard;
        private long nbEnAttente;
        private BigDecimal totalRemises;
        private BigDecimal totalPenalites;
        private long nbRelances;
    }

    @Data
    @Builder
    public static class LigneParMois {
        private String mois;
        private BigDecimal montantDu;
        private BigDecimal montantPaye;
        private BigDecimal solde;
        private long nbPaiements;
        private BigDecimal depenses;
    }

    @Data
    @Builder
    public static class LigneParClasse {
        private String classe;
        private long nbEleves;
        private BigDecimal montantDu;
        private BigDecimal montantPaye;
        private BigDecimal solde;
        private BigDecimal tauxRecouvrement;
    }

    @Data
    @Builder
    public static class LigneParEleve {
        private UUID studentId;
        private String nom;
        private String prenom;
        private String classe;
        private BigDecimal montantDu;
        private BigDecimal montantPaye;
        private BigDecimal solde;
        private String statut;
    }
}
