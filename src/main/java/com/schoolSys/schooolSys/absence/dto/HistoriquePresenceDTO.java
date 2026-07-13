package com.schoolSys.schooolSys.absence.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriquePresenceDTO {
    private UUID eleveId;
    private String studentName;
    private long totalAbsences;
    private long totalRetards;
    private long totalJustifiees;
    private double tauxPresence;
    private String alertLevel; // NORMAL, JAUNE, ROUGE
    private List<MonthlyBreakdown> monthlyBreakdown;
    private List<RecentAbsence> recentAbsences;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyBreakdown {
        private int month;
        private int year;
        private long absences;
        private long retards;
        private long justifiees;
        private double tauxPresence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentAbsence {
        private UUID id;
        private LocalDate date;
        private String type;
        private String seance;
        private LocalTime heureArrivee;
        private Boolean justifie;
        private String motif;
    }
}
