package com.schoolSys.schooolSys.parent.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated "what's coming up" payload for the parent home screen:
 * devoirs à rendre, examens et paiements dûs dans les N prochains jours.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingDTO {

    private List<UpcomingDevoir> devoirs;
    private List<UpcomingExamen> examens;
    private List<UpcomingPaiement> paiements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingDevoir {
        private UUID id;
        private String titre;
        private String moduleNom;
        private LocalDate dateLimite;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingExamen {
        private UUID id;
        private String name;
        private String moduleNom;
        private Integer trimestre;
        private LocalDate dateLimiteSaisie;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingPaiement {
        private UUID id;
        private String typeFraisNom;
        private String mois;
        private String anneeScolaire;
        private BigDecimal montantDu;
        private BigDecimal montantPaye;
        private String statut;
    }
}
