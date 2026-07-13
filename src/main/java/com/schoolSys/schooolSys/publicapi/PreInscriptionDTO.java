package com.schoolSys.schooolSys.publicapi;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PreInscriptionDTO {

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    private String lastName;

    private String firstNameAr;
    private String lastNameAr;

    @NotBlank(message = "Le sexe est requis")
    private String sex;

    private String dateOfBirth;
    private String birthPlace;

    private String niveau;
    private String classe;

    // Parent info
    private String parentLastName;
    private String parentFirstName;
    private String parentPhone;

    @Email(message = "Email parent invalide")
    private String parentEmail;

    private String notes;
}
