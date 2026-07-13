package com.schoolSys.schooolSys.teacher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TeacherRequestDTO {

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    private String lastName;

    private String email;

    @NotBlank(message = "La spécialité est requise")
    private String specialization;

    @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F")
    private String sexe;

    private String telephone;

    private String dateNaissance;

    @Pattern(regexp = "^(Actif|Inactif|En attente)$", message = "Statut invalide")
    private String statut;
}
