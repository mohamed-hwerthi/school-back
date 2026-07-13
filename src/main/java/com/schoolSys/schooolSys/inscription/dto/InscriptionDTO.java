package com.schoolSys.schooolSys.inscription.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionDTO {

    private UUID id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String sexe;
    private String adresse;
    private String telephoneParent;
    private String emailParent;
    private String nomParent;
    private String prenomParent;
    private UUID niveauId;
    private String niveauNom;
    private String anneeScolaire;
    private String statut;
    private String commentaire;
    private String numeroDossier;
    private String[] documentsPaths;
    private BigDecimal montantFrais;
    private Boolean fraisPaye;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
