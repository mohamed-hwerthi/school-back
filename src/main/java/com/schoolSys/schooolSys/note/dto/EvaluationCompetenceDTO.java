package com.schoolSys.schooolSys.note.dto;

import java.util.UUID;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EvaluationCompetenceDTO {
    private UUID id;
    private UUID eleveId;
    private UUID competenceId;
    private String competenceLabel;
    private UUID examenId;
    private String niveau; // NON_ATTEINT, EN_COURS, ATTEINT, DEPASSE
    private String commentaire;
}
