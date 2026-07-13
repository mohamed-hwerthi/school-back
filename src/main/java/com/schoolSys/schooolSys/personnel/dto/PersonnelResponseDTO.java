package com.schoolSys.schooolSys.personnel.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonnelResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String fonction;
    private String email;
    private String sexe;
    private String telephone;
    private String dateNaissance;
    private String dateEmbauche;
    private String statut;
}
