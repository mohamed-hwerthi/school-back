package com.schoolSys.schooolSys.volumehoraire.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleClasseVolumeRequestDTO {

    @NotNull(message = "Le module est obligatoire")
    private UUID moduleId;

    @NotNull(message = "La classe est obligatoire")
    private UUID classeId;

    private UUID enseignantId;

    private UUID anneeScolaireId;

    @NotNull(message = "Le nombre d'heures hebdomadaires est obligatoire")
    @Min(value = 1, message = "Au moins 1 heure par semaine")
    @Max(value = 20, message = "Au plus 20 heures par semaine")
    private Integer nbHeuresHebdo;
}
