package com.schoolSys.schooolSys.affectation.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Bulk teacher assignment: one teacher assigned to many (classe, module) pairs
 * in a single submit. The frontend expands the chosen niveaux/classes and the
 * chosen matière or domaine into explicit {@link Item}s.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffectationBulkRequestDTO {

    @NotNull
    private UUID teacherId;

    @NotNull
    private String anneeScolaire;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String notes;

    @NotEmpty
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @NotNull
        private UUID classeId;
        /** Nullable: prof principal sans matière dédiée. */
        private UUID moduleId;
    }
}
