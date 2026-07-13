package com.schoolSys.schooolSys.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentRequestDTO {

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    private String lastName;

    private String firstNameAr;
    private String lastNameAr;

    @NotBlank(message = "Le sexe est requis")
    @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F")
    private String sex;

    private LocalDate dateOfBirth;
    private String birthPlace;
    private String address;
    private String registrationNumber;

    @Email(message = "Email invalide")
    private String email;

    private String classe;
    private String niveau;
    private LocalDate enrollmentDate;

    private String status;
    private Boolean isBlocked;

    private String parentLastName;
    private String parentFirstName;
    private String parentPhone;

    @Email(message = "Email parent invalide")
    private String parentEmail;

    private String notes;
}
