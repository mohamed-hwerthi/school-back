package com.schoolSys.schooolSys.admin.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payloads pour le home admin — 6 KPI cards + alertes opérationnelles.
 */
public class AdminHomeDTO {

    /** MOB-FUNC-025 — 6 KPI agrégés pour l'accueil admin. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardKpis {
        /** Effectif total d'élèves actifs. */
        private Long effectifTotal;
        /** Taux d'occupation moyen des classes en %. */
        private Double tauxOccupation;
        /** Encaissements du mois en cours. */
        private BigDecimal caDuMois;
        /** Solde des impayés (en attente + en retard). */
        private BigDecimal impayes;
        /** Taux de présence école sur 30 derniers jours en %. */
        private Double tauxPresence;
        /** Taux de réussite global (élèves avec moyenne >= 10) en %. */
        private Double tauxReussite;
    }

    /** MOB-FUNC-028 — alertes opérationnelles à remonter sur l'accueil admin. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationalAlerts {
        /** Paiements EN_RETARD depuis plus de 30 jours. */
        private Integer impayes30j;
        /** Élèves avec > 10 absences ce mois. */
        private Integer absenteesCeMois;
        /** Classes sans aucune affectation enseignant. */
        private Integer classesSansAffectation;
    }
}
