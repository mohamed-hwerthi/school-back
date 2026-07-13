package com.schoolSys.schooolSys.parent.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MOB-FUNC-007 — notes par matière sur les 3 trimestres pour tracer la
 * progression d'un enfant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildProgressDTO {

    private List<MatiereProgress> matieres;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatiereProgress {
        private UUID moduleId;
        private String moduleNom;
        private Double t1;
        private Double t2;
        private Double t3;
        /** "haut", "stable", "bas" — calculé à partir de T3 vs (T1+T2)/2. */
        private String tendance;
    }
}
