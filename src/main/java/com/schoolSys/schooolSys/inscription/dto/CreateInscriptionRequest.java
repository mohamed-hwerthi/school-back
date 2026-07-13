package com.schoolSys.schooolSys.inscription.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class CreateInscriptionRequest {

    @NotBlank(message = "Le nom est requis")
    private String nom;

    @NotBlank(message = "Le prenom est requis")
    private String prenom;

    @NotNull(message = "La date de naissance est requise")
    private LocalDate dateNaissance;

    private String lieuNaissance;

    private String sexe;

    private String adresse;

    private String telephoneParent;

    @Email(message = "L'email parent est invalide")
    private String emailParent;

    private String nomParent;

    private String prenomParent;

    private UUID niveauId;

    @NotBlank(message = "L'annee scolaire est requise")
    private String anneeScolaire;
}
