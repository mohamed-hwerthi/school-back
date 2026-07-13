package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AttestationDTO {
    private UUID studentId;
    private String studentName;
    private String studentNameAr;
    private String dateOfBirth;
    private String birthPlace;
    private String registrationNumber;
    private String classe;
    private String niveau;
    private String anneeScolaire;
    private String schoolName;
    private String schoolNameAr;
    private String adresse;
    private String telephone;
    private String directeurName;
    private String directeurNameAr;
    private String dateGeneration;
}
