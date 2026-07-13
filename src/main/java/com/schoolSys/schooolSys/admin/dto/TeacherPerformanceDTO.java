package com.schoolSys.schooolSys.admin.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Performance d'un enseignant — alimente le tableau "Performance enseignants" admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPerformanceDTO {
    private UUID teacherId;
    private String prenom;
    private String nom;
    /** Nombre de classes affectées (année scolaire courante). */
    private Integer nbClasses;
    /** % d'examens du trimestre courant avec au moins une note saisie. */
    private Double saisiesAJour;
    /** Moyenne des notes données par cet enseignant ce trimestre. */
    private Double moyenneDonnee;
    /** Nombre de devoirs publiés ce trimestre. */
    private Integer nbDevoirs;
}
