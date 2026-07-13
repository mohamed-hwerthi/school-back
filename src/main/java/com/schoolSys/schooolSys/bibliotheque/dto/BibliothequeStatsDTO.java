package com.schoolSys.schooolSys.bibliotheque.dto;

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
public class BibliothequeStatsDTO {

    private long totalLivres;
    private long totalEmprunts;
    private long empruntsEnCours;
    private long empruntsEnRetard;
    private List<LivreEmprunteDTO> livresLesPlusEmpruntes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivreEmprunteDTO {
        private UUID livreId;
        private String titre;
        private long count;
    }
}
