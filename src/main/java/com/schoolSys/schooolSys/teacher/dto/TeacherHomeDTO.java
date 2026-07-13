package com.schoolSys.schooolSys.teacher.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payloads enrichis pour le home enseignant — cartes "À corriger",
 * "Élèves à risque" et "Saisies en retard".
 */
public class TeacherHomeDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingCorrections {
        private Integer devoirs;
        private Integer quiz;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAtRisk {
        private UUID studentId;
        private String prenom;
        private String nom;
        private String classeNom;
        private String motif;   // "moyenne_faible" | "absences_repetees"
        private Double valeur;  // moyenne (sur 20) ou nombre d'absences
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingTask {
        private String kind;        // "PRESENCE" | "NOTE"
        private LocalDate date;
        private String classeNom;
        private String moduleNom;
        private String label;       // description prête à afficher
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingTasks {
        private List<PendingTask> items;
        private Integer total;
    }
}
