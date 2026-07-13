package com.schoolSys.schooolSys.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantOnboardingRequest {

    @NotBlank(message = "Le nom de l'école (latin) est obligatoire")
    private String schoolName;

    @NotBlank(message = "Le nom de l'école en arabe est obligatoire")
    private String schoolNameAr;

    @NotBlank(message = "La délégation régionale est obligatoire")
    private String delegationRegionale;

    @NotBlank(message = "La délégation régionale en arabe est obligatoire")
    private String delegationRegionaleAr;

    private String slug;
    private String adminFirstName;
    private String adminLastName;
    private String adminEmail;
    private String adminPassword;
    private String plan;
    private String contactPhone;
}
