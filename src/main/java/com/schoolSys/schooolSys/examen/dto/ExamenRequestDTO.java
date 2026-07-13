package com.schoolSys.schooolSys.examen.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class ExamenRequestDTO {

    @NotBlank(message = "Le nom de l'examen est obligatoire")
    private String name;

    private String namePrive;

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

    @NotNull(message = "Le trimestre est obligatoire")
    @Min(value = 1, message = "Le trimestre doit être entre 1 et 3")
    @Max(value = 3, message = "Le trimestre doit être entre 1 et 3")
    @Builder.Default
    private Integer trimestre = 1;

    @NotNull(message = "La classe est obligatoire")
    private UUID classeId;

    private UUID teacherId;

    @NotNull(message = "Le module est obligatoire")
    private UUID moduleId;

    @Builder.Default
    private Boolean versionEtatique = true;

    @Builder.Default
    private Boolean versionPrivee = true;
}
