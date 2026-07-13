package com.schoolSys.schooolSys.examenonline.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitReponseRequest {

    @NotNull(message = "La tentative est obligatoire")
    private UUID tentativeId;

    private List<ReponseItem> reponses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReponseItem {
        private UUID questionId;
        private UUID choixId;
        private String reponseTexte;
    }
}
