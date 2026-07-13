package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StatsReussiteDTO {
    private String classe;
    private Integer trimestre;
    private int totalEleves;
    private int reussis;
    private int echoues;
    private double tauxReussite;
    private double moyenneClasse;
    private double moyenneMin;
    private double moyenneMax;
    private List<ModuleStatsDTO> modulesStats;
    private List<DistributionDTO> distribution;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ModuleStatsDTO {
        private UUID moduleId;
        private String moduleName;
        private double moyenne;
        private double min;
        private double max;
        private int reussis;
        private int echoues;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DistributionDTO {
        private String range;
        private int count;
    }
}
