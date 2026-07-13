package com.schoolSys.schooolSys.examenonline.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTentativeRequest {

    @NotNull(message = "Le quiz est obligatoire")
    private UUID quizId;

    @NotNull(message = "L'eleve est obligatoire")
    private UUID eleveId;
}
