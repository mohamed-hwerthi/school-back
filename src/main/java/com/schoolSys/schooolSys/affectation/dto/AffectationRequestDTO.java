package com.schoolSys.schooolSys.affectation.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffectationRequestDTO {

    @NotNull
    private UUID teacherId;

    @NotNull
    private UUID classeId;

    /** Nullable: prof principal sans matière dédiée. */
    private UUID moduleId;

    @NotNull
    private String anneeScolaire;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String notes;
}
