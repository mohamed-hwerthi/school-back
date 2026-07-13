package com.schoolSys.schooolSys.analytics.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ComparaisonClassesDTO {

    private List<ClasseStats> classes;

    @Data
    @Builder
    public static class ClasseStats {
        private UUID classeId;
        private String classeName;
        private double moyenne;
        private double tauxReussite;
        private double tauxPresence;
        private long effectif;
    }
}
