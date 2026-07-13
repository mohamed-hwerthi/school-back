package com.schoolSys.schooolSys.parent.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.schoolSys.schooolSys.bulletin.dto.BulletinDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MOB-FUNC-010 — profil enfant complet : infos administratives,
 * bulletin du trimestre en cours, classement et appréciations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildProfileDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String matricule;
    private String niveau;
    private String classe;
    private String sexe;
    private LocalDate dateOfBirth;
    private LocalDate enrollmentDate;
    private String status;
    /** Bulletin du trimestre courant (optionnel — null si pas encore disponible). */
    private BulletinDTO currentBulletin;
    /** Rang dans la classe (calculé sur la moyenne du trimestre courant). */
    private Integer rangClasse;
    /** Effectif total de la classe. */
    private Integer effectifClasse;
    /** Moyenne générale du trimestre courant. */
    private Double moyenneTrimestre;
}
