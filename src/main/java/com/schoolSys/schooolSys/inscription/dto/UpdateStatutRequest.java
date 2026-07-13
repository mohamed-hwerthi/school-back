package com.schoolSys.schooolSys.inscription.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatutRequest {

    @NotBlank(message = "Le statut est requis")
    private String statut;

    private String commentaire;
}
