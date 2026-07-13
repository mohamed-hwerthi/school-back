package com.schoolSys.schooolSys.domaine.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SousDomaineRequestDTO {

    @NotBlank(message = "Le nom du sous-domaine est obligatoire")
    private String name;

    private String nameAr;

    @NotNull
    @Builder.Default
    private Integer ordre = 1;

    @NotNull(message = "Le domaine est obligatoire")
    private UUID domaineId;
}
