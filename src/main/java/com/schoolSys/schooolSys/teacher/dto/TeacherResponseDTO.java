package com.schoolSys.schooolSys.teacher.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String specialization;
    private String sexe;
    private String telephone;
    private String dateNaissance;
    private String dateEmbauche;
    private String statut;
}
