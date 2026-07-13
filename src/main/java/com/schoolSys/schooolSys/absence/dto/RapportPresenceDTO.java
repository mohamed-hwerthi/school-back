package com.schoolSys.schooolSys.absence.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapportPresenceDTO {
    private String classeLabel;
    private int mois;
    private int annee;
    private long totalEleves;
    private long totalAbsences;
    private long totalRetards;
    private double tauxPresenceGlobal;
    private List<ElevePresenceDTO> eleves;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElevePresenceDTO {
        private UUID eleveId;
        private String nom;
        private String prenom;
        private long totalAbsences;
        private long totalRetards;
        private long totalJustifiees;
        private double tauxPresence;
    }
}
