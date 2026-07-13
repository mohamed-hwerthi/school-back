package com.schoolSys.schooolSys.personnel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PersonnelRequestDTO {

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    private String lastName;

    @NotBlank(message = "La fonction est requise")
    private String fonction;

    private String email;

    @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F")
    private String sexe;

    private String telephone;

    private String dateNaissance;

    @Pattern(regexp = "^(Actif|Inactif|En attente)$", message = "Statut invalide")
    private String statut;
}
