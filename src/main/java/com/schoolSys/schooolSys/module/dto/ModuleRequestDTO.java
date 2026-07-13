package com.schoolSys.schooolSys.module.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleRequestDTO {

    @NotBlank(message = "Le nom du module est obligatoire")
    private String name;

    private String nameVp;

    private String nameAr;

    @NotNull
    @Builder.Default
    private Double coeffEtatique = 1.0;

    @NotNull
    @Builder.Default
    private Double coeffPrive = 1.0;

    @NotNull
    @Builder.Default
    private Integer ordreEtatique = 1;

    @NotNull
    @Builder.Default
    private Integer ordrePrive = 1;

    @NotNull(message = "Le niveau est obligatoire")
    private UUID niveauId;

    @NotNull(message = "Le domaine est obligatoire")
    private UUID domaineId;

    private UUID sousDomaineId;

    @Builder.Default
    private Boolean versionEtatique = true;

    @Builder.Default
    private Boolean versionPrivee = true;

    @Builder.Default
    private String salleTypeRequise = "NORMAL";

    @Builder.Default
    private Integer dureeMinSeance = 1;

    @Builder.Default
    private Integer dureeMaxSeance = 2;

    @Builder.Default
    private String preferenceHoraire = "INDIFFERENT";
}
