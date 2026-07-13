package com.schoolSys.schooolSys.note.dto;

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
public class NoteRequestDTO {

    @NotNull(message = "L'élève est obligatoire")
    private UUID studentId;

    @NotNull(message = "L'examen est obligatoire")
    private UUID examenId;

    @NotNull(message = "Le trimestre est obligatoire")
    @Min(1)
    @Max(3)
    private Integer trimestre;

    @Min(0)
    @Max(20)
    private Double valeur;

    private String observation;

    private String statut;
}
