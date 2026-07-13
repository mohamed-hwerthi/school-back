package com.schoolSys.schooolSys.discipline.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierDisciplinaireDTO {
    private UUID eleveId;
    private String eleveNom;
    private int totalIncidents;
    private int totalSanctions;
    private int niveauActuel; // highest active sanction level
    private List<EvenementDisciplinaireDTO> timeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvenementDisciplinaireDTO {
        private LocalDateTime date;
        private String type; // INCIDENT or SANCTION
        private String description;
        private String gravite;   // for incidents
        private Integer niveau;   // for sanctions
        private String statut;    // for sanctions
        private UUID id;
    }
}
