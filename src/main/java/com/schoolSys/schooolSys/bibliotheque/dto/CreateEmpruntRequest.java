package com.schoolSys.schooolSys.bibliotheque.dto;

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
public class CreateEmpruntRequest {

    @NotNull(message = "L'ID du livre est obligatoire")
    private UUID livreId;

    @NotNull(message = "L'ID de l'eleve est obligatoire")
    private UUID eleveId;

    @NotNull(message = "La date de retour prevue est obligatoire")
    private LocalDate dateRetourPrevue;

    private String notes;
}
