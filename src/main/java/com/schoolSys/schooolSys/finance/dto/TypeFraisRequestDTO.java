package com.schoolSys.schooolSys.finance.dto;

import com.schoolSys.schooolSys.finance.TypeFrais;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TypeFraisRequestDTO {

    @NotBlank(message = "Le nom est requis")
    private String nom;

    @NotNull(message = "Le montant est requis")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "La fréquence est requise")
    private TypeFrais.Frequence frequence;

    private String description;

    private Boolean actif;
}
