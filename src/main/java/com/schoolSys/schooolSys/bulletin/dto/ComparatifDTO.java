package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ComparatifDTO {
    private List<ClassePerformanceDTO> classesPerformance;
    private List<EvolutionTrimestreDTO> evolution;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClassePerformanceDTO {
        private UUID classeId;
        private String classeName;
        private double moyenneGenerale;
        private double tauxReussite;
        private int totalEleves;
        private int reussis;
        private List<ModuleAvgDTO> modulesAvg;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ModuleAvgDTO {
        private UUID moduleId;
        private String moduleName;
        private double moyenne;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EvolutionTrimestreDTO {
        private int trimestre;
        private double moyenneGenerale;
        private double tauxReussite;
        private int totalEleves;
        private int reussis;
    }
}
