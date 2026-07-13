package com.schoolSys.schooolSys.examenonline.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    private UUID id;
    private UUID quizId;
    private String texte;
    private String typeQuestion;
    private BigDecimal points;
    private Integer ordre;
    private String explication;
    private String imageUrl;
    private Boolean obligatoire;
    private List<ChoixReponseDTO> choix;
}
