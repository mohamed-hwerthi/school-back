package com.schoolSys.schooolSys.examenonline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateQuestionRequest {

    @NotBlank(message = "Le texte de la question est obligatoire")
    private String texte;

    @NotNull(message = "Le type de question est obligatoire")
    private String typeQuestion;

    @Builder.Default
    private BigDecimal points = BigDecimal.ONE;

    @NotNull(message = "L'ordre est obligatoire")
    private Integer ordre;

    private String explication;

    private String imageUrl;

    @Builder.Default
    private Boolean obligatoire = true;

    private List<ChoixReponseDTO> choix;
}
