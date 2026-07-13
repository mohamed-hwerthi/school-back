package com.schoolSys.schooolSys.reporting.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payloads de drill-down pour une classe — histogramme de distribution,
 * évolution trimestrielle et top/flop des élèves.
 */
public class ClassDrillDownDTO {

    /** MOB-FUNC-016 — distribution des notes par tranche. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeDistribution {
        private List<GradeBucket> buckets;
        private Integer totalNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeBucket {
        private String range;   // "0-5", "5-10", "10-15", "15-20"
        private Integer count;
    }

    /** MOB-FUNC-017 — moyennes par trimestre avec comparaison à la moyenne école. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrimestreEvolution {
        private List<TrimestrePoint> points;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrimestrePoint {
        private Integer trimestre;
        private Double moyenne;
        private Double tauxReussite;
        private Double tauxPresence;
        private Double moyenneEcole;
    }

    /** MOB-FUNC-018 — top 5 et flop 5 élèves de la classe pour un trimestre. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopFlop {
        private List<StudentRank> top;
        private List<StudentRank> flop;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRank {
        private UUID studentId;
        private String prenom;
        private String nom;
        private Double moyenne;
        private Integer rang;
    }

    /** MOB-FUNC-029 — liste complète des élèves d'une classe avec leurs stats. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentStats {
        private UUID studentId;
        private String prenom;
        private String nom;
        private String matricule;
        private Double moyenne;
        private Integer rang;
        private Integer totalAbsences;
        private Integer totalRetards;
    }
}
