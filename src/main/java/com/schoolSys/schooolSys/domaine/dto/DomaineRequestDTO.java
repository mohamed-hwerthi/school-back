package com.schoolSys.schooolSys.domaine.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomaineRequestDTO {

    @NotBlank(message = "Le nom du domaine est obligatoire")
    private String name;

    private String nameAr;

    @NotNull
    @Builder.Default
    private Integer ordre = 1;

    @Builder.Default
    private Double coeffEtatique = 1.0;

    @Builder.Default
    private Double coeffPrive = 1.0;

    @Builder.Default
    private Boolean versionEtatique = true;

    @Builder.Default
    private Boolean versionPrivee = true;

    @NotNull(message = "Le niveau est obligatoire")
    private UUID niveauId;
}
