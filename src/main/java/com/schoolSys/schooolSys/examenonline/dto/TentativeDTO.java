package com.schoolSys.schooolSys.examenonline.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TentativeDTO {

    private UUID id;
    private UUID quizId;
    private String quizTitre;
    private UUID eleveId;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private BigDecimal score;
    private BigDecimal scorePourcentage;
    private String statut;
    private Integer tempsPasseSecondes;
    private List<ReponseEleveDTO> reponses;
    private LocalDateTime createdAt;
}
