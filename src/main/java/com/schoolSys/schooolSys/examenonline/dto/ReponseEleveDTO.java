package com.schoolSys.schooolSys.examenonline.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReponseEleveDTO {

    private UUID id;
    private UUID tentativeId;
    private UUID questionId;
    private String questionTexte;
    private UUID choixId;
    private String reponseTexte;
    private Boolean correct;
    private BigDecimal pointsObtenus;
}
