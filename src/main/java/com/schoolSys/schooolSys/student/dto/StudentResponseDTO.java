package com.schoolSys.schooolSys.student.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StudentResponseDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String firstNameAr;
    private String lastNameAr;
    private String sex;
    private LocalDate dateOfBirth;
    private String birthPlace;
    private String address;
    private String registrationNumber;
    private String email;
    private String classe;
    private String niveau;
    private LocalDate enrollmentDate;
    private String status;
    private Boolean isBlocked;
    private String parentLastName;
    private String parentFirstName;
    private String parentPhone;
    private String parentEmail;
    private String notes;
    private String matricule;
}
