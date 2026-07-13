package com.schoolSys.schooolSys.disponibilite.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantDisponibiliteRequestDTO {

    @NotNull(message = "L'enseignant est obligatoire")
    private UUID enseignantId;

    @NotNull(message = "Le jour est obligatoire")
    @Min(value = 1, message = "Jour entre 1 (lundi) et 6 (samedi)")
    @Max(value = 6, message = "Jour entre 1 (lundi) et 6 (samedi)")
    private Integer jourSemaine;

    @NotNull(message = "Le créneau est obligatoire")
    private UUID creneauId;

    @NotNull(message = "Le type est obligatoire")
    @Pattern(regexp = "INDISPONIBLE|PREFERE|EVITER",
             message = "Type doit être INDISPONIBLE, PREFERE ou EVITER")
    private String type;

    @Size(max = 200)
    private String motif;
}
